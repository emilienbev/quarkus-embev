#!/bin/bash

# Set directories as variables
SRC_RUNTIME="quarkus/extensions/grpc-common/runtime/src/main/java/io/quarkus/grpc/common"
DEST_RUNTIME="runtime/src/main/java/com/couchbase/quarkus/extension/runtime/grpchandling"

SRC_DEPLOYMENT="quarkus/extensions/grpc-common/deployment/src/main/java/io/quarkus/grpc/common/deployment"
DEST_DEPLOYMENT="deployment/src/main/java/com/couchbase/quarkus/extension/deployment/grpchandling"

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
git sparse-checkout set extensions/grpc-common
cd ..

# Creating "grpc-handling" directories to keep pulled files separate from our extension's
echo "2 - Creating grpc-handling directories"
mkdir -p "$DEST_DEPLOYMENT"
mkdir -p "$DEST_RUNTIME"

# Copy files from runtime and deployment directories and overwrite existing
echo "3 - Copying files"
cp -r "$SRC_RUNTIME"/* "$DEST_RUNTIME"
cp -r "$SRC_DEPLOYMENT"/* "$DEST_DEPLOYMENT"

# Prepend "com.couchbase.client.core.deps." to all "io.netty" occurrences in the copied files
echo "4 - Replacing shaded grpc-handling namespace"
find "$DEST_RUNTIME" "$DEST_DEPLOYMENT" -type f -name "*.java" -exec sed -i '' 's/io\.grpc/com.couchbase.client.core.deps.io.grpc/g' "{}" +
find "$DEST_RUNTIME" "$DEST_DEPLOYMENT" -type f -name "*.java" -exec sed -i '' 's/com\.google/com.couchbase.client.core.deps.com.google/g' "{}" +

# Fix the imports and packages
echo "5 - Fixing imports and packages"
find "$DEST_RUNTIME" "$DEST_DEPLOYMENT" -type f -name "*.java" -exec sed -i '' \
    -e 's/io\.quarkus\.grpc\.common\.deployment/com.couchbase.quarkus.extension.deployment.grpchandling/g' \
    -e 's/io\.quarkus\.grpc\.common/com.couchbase.quarkus.extension.runtime.grpchandling/g' \
    -e 's/io\.quarkus\.grpc\.common\.runtime/com.couchbase.quarkus.extension.runtime.grpchandling.runtime/g' \
    -e 's/io\.quarkus\.grpc\.common\.runtime\.virtual/com.couchbase.quarkus.extension.runtime.grpchandling.runtime.virtual/g' \
    -e 's/io\.quarkus\.grpc\.common\.runtime\.graal/com.couchbase.quarkus.extension.runtime.grpchandling.runtime.graal/g' \
    "{}" +

# Delete the cloned repo
echo "9 - Deleting cloned repo"
rm -rf "quarkus"

echo "10 - Done!"
