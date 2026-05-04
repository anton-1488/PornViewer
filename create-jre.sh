rm -rf ./files/pv-jre
jlink --add-modules java.base,jdk.crypto.ec,jdk.httpserver,java.desktop,java.logging,java.net.http,java.sql,jdk.unsupported --strip-debug --no-header-files --no-man-pages --compress=zip-9 --output ./files/pv-jre
rm -rf ./files/pv-jre/legal

clear
du -sh ./files/pv-jre
./files/pv-jre/bin/java -jar --enable-native-access=ALL-UNNAMED ./input-pv/PornViewer.jar