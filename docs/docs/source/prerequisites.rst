Prerequisites
=============
.. Note::
    This section provides the list of software required and how to verify them prior to getting started.

1. Java and JDK: 8
~~~~~~~~~~~~~~~~~

.. code-block:: powershell

    java -version
    # openjdk version "1.8.0_402"

    javac -version
    # javac 1.8.0_402

2. Maven: 3.9+
~~~~~~~~~~~~~

.. code-block:: powershell

    mvn -version
    # Apache Maven 3.9.0

3. NodeJS: 14.21.0+
~~~~~~~~~~~~~~~~~

.. code-block:: powershell

    node -v
    # v15.0.0

4. Python: 3.10
~~~~~~~~~~~~~~~~~

.. code-block:: powershell

    python --version
    # Python 3.10.0

5. PostgreSQL: 15.0+
~~~~~~~~~~~~~~~~~~~

.. code-block:: powershell

    psql --version
    # psql (PostgreSQL) 15.0


6. Apache Tomcat: 9.0.98
~~~~~~~~~~~~~~~~~~~~~~~~

.. Note::
    Assumption is that Apache Tomcat is installed at `C:\\ProgramFiles\\apache-tomcat-9.0.98`

.. code-block:: powershell

    C:\ProgramFiles\apache-tomcat-9.0.98\bin\version.bat
    # Server version: Apache Tomcat/9.0.98


Verify that server is up and running: http://localhost:8080/

7. Nginx 1.20+
~~~~~~~~~~~~~~

.. Note::
    Assumption is that nginx is installed at `C:\\ProgramFiles\\nginx-1.27.3`

.. code-block:: powershell

    nginx -version
    # nginx version: nginx/1.27.3

8. Tesseract >= 5.0 
~~~~~~~~~~~~~~~~~~~
Ref: https://tesseract-ocr.github.io

.. Note::
    Assumption is that Tesseract is installed at `C:/Program Files/Tesseract-OCR/tesseract.exe`
    
It is optional,if you want you can use Azure Read subscription.

