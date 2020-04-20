Set objShell = CreateObject("WScript.Shell")
Set objEndTask = objShell.Exec("net stop CedarAgent")
Set objStdOut = objEndTask.StdOut
objStdOut.readall

Set objEndTask = objShell.Exec("schtasks /End /TN cedar")
Set objStdOut = objEndTask.StdOut
objStdOut.readall