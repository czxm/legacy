#include <ScreenCapture.au3>

Dim $user = "qzhang8"
Dim $passwd = "Front123"
If $CmdLine[0] == 2 Then
    $user = $CmdLine[1]
    $passwd = $CmdLine[2]
EndIf

Send("{ESCAPE}")
ConsoleWrite("++Start Microsoft Outlook 2007" & @CRLF)
Run ('C:\Program Files\Microsoft Office\Office12\OUTLOOK.EXE')
WinWaitActive("Inbox - Microsoft Outlook", "")
_ScreenCapture_Capture("C:\start_outlook.png")

ConsoleWrite("++Open Salesforce Configuration Dialog" & @CRLF)
Send("{ALTDOWN}T{ALTUP}SS{ENTER}")
Sleep(2000)
WinWaitActive("Connect for Microsoft Outlook Configuration Options")
_ScreenCapture_Capture("C:\open_sfdc.png")

ControlSetText("Connect for Microsoft Outlook Configuration Options", "", "[CLASS:Edit;INSTANCE:1]", $user)
ControlSetText("Connect for Microsoft Outlook Configuration Options", "", "[CLASS:Edit;INSTANCE:2]", $passwd)
ControlClick("Connect for Microsoft Outlook Configuration Options", "", "[CLASS:Button;INSTANCE:2]")
$tsc = @SEC
WaitActive("Connect for Microsoft Outlook", "", "[CLASS:Static;INSTANCE:2]")
$tsc = GetTSC($tsc, @SEC)
$result = ControlGetText("Connect for Microsoft Outlook", "", "[CLASS:Static;INSTANCE:2]")
Sleep(2000)
_ScreenCapture_Capture("C:\sfdc_logon.png")
Send("{ENTER}{ALTDOWN}{F4}{ALTUP}")
If $result <> "Successfully logged in to salesforce.com." Then
	ConsoleWrite("++Logon failed (" & $tsc & "s)" & @CRLF)
	WinClose("Inbox - Microsoft Outlook","")
	Exit 1
Else
	ConsoleWrite("++Logon succeeded (" & $tsc & "s)" & @CRLF)
EndIf

Sleep(1000)
ControlClick("Inbox - Microsoft Outlook", "", "[CLASS:MsoCommandBar;INSTANCE:2]", "main", 1, 420, 5)
$tsc = @SEC
WaitActive("Confirm Additions", "", "[CLASS:Static;INSTANCE:1]")
ConsoleWrite("++Sync1 (" & GetTSC($tsc, @SEC) & "s)" & @CRLF)
_ScreenCapture_Capture("C:\sfdc_sync1.png")

ControlClick("Confirm Additions", "", "[CLASS:Button;INSTANCE:1]")
$tsc = @SEC
WaitActive("Confirm Additions", "", "[CLASS:Static;INSTANCE:1]")
ConsoleWrite("++Sync2 (" & GetTSC($tsc, @SEC) & "s)" & @CRLF)
_ScreenCapture_Capture("C:\sfdc_sync2.png")

ControlClick("Confirm Additions", "", "[CLASS:Button;INSTANCE:1]")
$tsc = @SEC
WaitActive("Confirm Additions", "", "[CLASS:Static;INSTANCE:1]")
ConsoleWrite("++Sync3 (" & GetTSC($tsc, @SEC) & "s)" & @CRLF)
_ScreenCapture_Capture("C:\sfdc_sync3.png")

ControlClick("Confirm Additions", "", "[CLASS:Button;INSTANCE:1]")
WaitActive("Synchronization Completed", "", "[CLASS:Button;INSTANCE:1]")
Send("{ENTER}")
Sleep(1000)
_ScreenCapture_Capture("C:\finish_outlook.png")
WinClose("Inbox - Microsoft Outlook","")

Func WaitActive($title, $text, $id)
    $result = ControlGetText($title, $text, $id)
	While $result == ""
		Sleep(1000)
	    $result = ControlGetText($title, $text, $id)
	WEnd
EndFunc

Func GetTSC($before, $now)
    If $now < $before Then
		Return ( 60 - $before ) + $now
	Else
		Return $now - $before
	EndIf
EndFunc