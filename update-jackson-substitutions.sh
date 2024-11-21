#!/bin/bash

# Set directories as variables
SRC_RUNTIME="quarkus/extensions/jackson/runtime/src/main/java/io/quarkus/jackson"
DEST_RUNTIME="runtime/src/main/java/com/couchbase/quarkus/extension/runtime/jacksonhandling"

SRC_DEPLOYMENT="quarkus/extensions/jackson/deployment/src/main/java/io/quarkus/jackson/deployment"
DEST_DEPLOYMENT="deployment/src/main/java/com/couchbase/quarkus/extension/deployment/jacksonhandling"

SRC_SPI="quarkus/extensions/jackson/deployment/src/main/java/io/quarkus/jackson/deployment"

# Get Quarkus version from parent pom.xml and use it to checkout the correct Quarkus branch for netty substitutions
POM_FILE=$(find . -maxdepth 1 -name "pom.xml" -print -quit)
QUARKUS_VERSION=$(sed -n 's/.*<quarkus.version>\(.*\)<\/quarkus.version>.*/\1/p' "$POM_FILE")

if [[ -z "$QUARKUS_VERSION" ]]; then
    echo "Could not find <quarkus.version> in pom.xml."
    exit 1
fi
echo "Found Quarkus version: $QUARKUS_VERSION"

# Clone repo
echo "1 - Cloning Quarkus"
git clone --depth=1 --filter=blob:none --sparse --branch "$QUARKUS_VERSION" git@github.com:quarkusio/quarkus.git
cd quarkus
git sparse-checkout set extensions/jackson bom/application
cd ..

BOM_FILE="quarkus/bom/application/pom.xml"
JACKSON_VERSION=$(sed -n 's/.*<jackson-bom.version>\(.*\)<\/jackson-bom.version>.*/\1/p' "$BOM_FILE")

if [[ -z "$JACKSON_VERSION" ]]; then
    echo "Could not find <netty.version> in the BOM file."
else
    echo "The Jackson target version is: $JACKSON_VERSION"
fi

# Creating "nettyhandling" directories to keep pulled files separate from our extension's
echo "2 - Creating nettyhandling directories"
mkdir -p "$DEST_DEPLOYMENT"
mkdir -p "$DEST_RUNTIME"

# Copy files from runtime and deployment directories and overwrite existing
echo "3 - Copying files"
cp -r "$SRC_RUNTIME"/* "$DEST_RUNTIME"
cp -r "$SRC_DEPLOYMENT"/* "$DEST_DEPLOYMENT"
cp -r "$SRC_SPI"/* "$DEST_RUNTIME"

# Prepend "com.couchbase.client.core.deps." to all "io.netty" occurrences in the copied files
echo "4 - Replacing shaded netty namespace"
find "$DEST_RUNTIME" "$DEST_DEPLOYMENT" -type f -name "*.java" -exec sed -i '' 's/com\.fasterxml\.jackson/com.couchbase.client.core.deps.com.fasterxml.jackson/g' "{}" +

# Fix the imports and packages
echo "5 - Fixing imports and packages"
find "$DEST_RUNTIME" "$DEST_DEPLOYMENT" -type f -name "*.java" -exec sed -i '' \
    -e 's/io\.quarkus\.jackson\.deployment/com.couchbase.quarkus.extension.deployment.jacksonhandling/g' \
    -e 's/io\.quarkus\.jackson/com.couchbase.quarkus.extension.runtime.jacksonhandling/g' \
    -e 's/io\.quarkus\.jackson\.runtime/com.couchbase.quarkus.extension.runtime.jacksonhandling.runtime/g' \
    "{}" +

# Delete the cloned repo
echo "9 - Deleting cloned repo"
rm -rf "quarkus"

echo "10 - Done!"
