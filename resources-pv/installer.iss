[Setup]
AppName=PornViewer
AppVersion=1.7.7
DefaultDirName={pf}\PornViewer
DefaultGroupName=PornViewer
UninstallDisplayIcon={app}\PornViewer.exe
Compression=lzma2
SolidCompression=yes
OutputDir=..\installer
OutputBaseFilename=PornViewer-1.7.7
PrivilegesRequired=admin
LicenseFile=..\files\LICENSE.txt
SetupIconFile=..\files\PornViewer.ico

[Files]
Source: "..\installer\PornViewer\*"; DestDir: "{app}"; Flags: recursesubdirs

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Icons]
Name: "{group}\PornViewer"; Filename: "{app}\PornViewer.exe"
Name: "{commondesktop}\PornViewer"; Filename: "{app}\PornViewer.exe"; Tasks: desktopicon

[Registry]
Root: HKCR; Subkey: "pv"; ValueType: string; ValueData: "URL:PornViewer Protocol"; Flags: uninsdeletekey
Root: HKCR; Subkey: "pv"; ValueName: "URL Protocol"; ValueType: string; ValueData: ""
Root: HKCR; Subkey: "pv\DefaultIcon"; ValueType: string; ValueData: "{app}\PornViewer.exe,0"
Root: HKCR; Subkey: "pv\shell\open\command"; ValueType: string; ValueData: """{app}\PornViewer.exe"" ""%1"""
Root: HKCR; Subkey: "pornviewer"; ValueType: string; ValueData: "URL:PornViewer Protocol"; Flags: uninsdeletekey
Root: HKCR; Subkey: "pornviewer"; ValueName: "URL Protocol"; ValueType: string; ValueData: ""
Root: HKCR; Subkey: "pornviewer\shell\open\command"; ValueType: string; ValueData: """{app}\PornViewer.exe"" ""%1"""


[Run]
Filename: "{app}\PornViewer.exe"; Description: "Launch PornViewer"; Flags: postinstall nowait