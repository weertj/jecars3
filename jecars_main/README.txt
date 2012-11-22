
JeCARS Core
-----------

The JeCARS is used by the JeCARS web application.

It has also a standalone application;

JeCARS BackupTool
=================

This is a GUI application which can import and export a JeCARS web repository.
It can also import and export a Jackrabbit standalone JCR repository.

The tool is started with;

java -cp jecars-core-0.20.jar org.jecars.backup.JB_BackupTool

Export a repository
-------------------

First select a (empty) export directory with "Select Im/Export Directory".

1. JeCARS
    Enter the correct URL and "Administrator" user and press on Export

2. Jackrabbit
    Select the Jackrabbit repository directory and press on Export

The backup tool is still under development but it supports;

- Nodetype export
- Namespace export
- Binary data is seperatly exported
- Node/property export
- Version export (experimental)

Import a repository
-------------------

First select a filled import directory with "Select Im/Export Directory".

1. JeCARS
    Enter the correct URL and "Administrator" user and press on Import

2. Jackrabbit
    Select the Jackrabbit repository directory and press on Import

The backup tool is still under development but it supports;



More Information
----------------

For more information how to use JeCARS goto http://jecars.wiki.sourceforge.net/


General JeCARS information and documentation at;

    http://jecars.sourceforge.net/



