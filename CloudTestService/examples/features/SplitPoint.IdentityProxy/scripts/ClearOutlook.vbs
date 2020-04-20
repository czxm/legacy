Option Explicit

Dim intMax, intOld
Dim objFolder, objItem, objNamespace, objOutlook

Const FolderTasks = 13
Const FolderContacts = 10
Const FolderCalendar = 9

Set objOutlook = CreateObject( "Outlook.Application" )
Set objNamespace = objOutlook.GetNamespace( "MAPI" )
objNamespace.Logon "personal", , False, False    
Set objFolder = objNamespace.GetDefaultFolder(FolderTasks)
For Each objItem In objFolder.Items
    objItem.Delete
Next
Set objFolder    = Nothing
Set objFolder = objNamespace.GetDefaultFolder(FolderCalendar)
For Each objItem In objFolder.Items
    objItem.Delete
Next
Set objFolder    = Nothing
Set objFolder = objNamespace.GetDefaultFolder(FolderContacts)
For Each objItem In objFolder.Items
    objItem.Delete
Next
Set objFolder    = Nothing
Set objNamespace = Nothing
Set objOutlook   = Nothing

