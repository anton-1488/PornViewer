#!/bin/bash

rm -rf ./files/pv-jre

jlink --add-modules java.base,jdk.crypto.ec,jdk.httpserver,java.desktop,java.logging,java.net.http,java.sql,jdk.unsupported \
      --strip-debug \
      --no-header-files \
      --no-man-pages \
      --compress=zip-9 \
      --output ./files/pv-jre

rm -rf ./files/pv-jre/legal

chmod +w ./files/pv-jre/lib/libawt.dylib
cp /Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home/lib/libawt.dylib ./files/pv-jre/lib/libawt.dylib

chmod +w ./files/pv-jre/lib/libawt_lwawt.dylib
cp /Library/Java/JavaVirtualMachines/jdk-25.jdk/Contents/Home/lib/libawt_lwawt.dylib ./files/pv-jre/lib/libawt_lwawt.dylib

install_name_tool -change "/System/Library/Frameworks/JavaRuntimeSupport.framework/Versions/A/JavaRuntimeSupport" \
                         "/System/Library/Frameworks/JavaVM.framework/JavaVM" \
                         ./files/pv-jre/lib/libawt_lwawt.dylib
codesign --force -s - ./files/pv-jre/lib/libawt_lwawt.dylib

chmod +w ./files/pv-jre/lib/libosxapp.dylib
install_name_tool -change "/System/Library/Frameworks/JavaRuntimeSupport.framework/Versions/A/JavaRuntimeSupport" \
                         "/System/Library/Frameworks/JavaVM.framework/JavaVM" \
                         ./files/pv-jre/lib/libosxapp.dylib
codesign --force -s - ./files/pv-jre/lib/libosxapp.dylib

clear
du -sh ./files/pv-jre
./files/pv-jre/bin/java --enable-native-access=ALL-UNNAMED -jar ./input-pv/PornViewer.jar