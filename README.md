# Batch Email Sender

# Introduction
Send templated emails with replaceable parameters to hundreds of recipients with ease using this library and tool!

With Batch Email Sender, you can send emails:
- to a large number of people with email addresses coming from a CSV file
- with customized greetings using values from a CSV file
- with customized subjects with values from a CSV file
- with email bodies containing parameters that are replaced by values from a CSV file
- with customized attachments whose names (paths to the files) come from a CSV file
- with attachments that are different for each recipient, with the file paths coming from a CSV file

This tool comes with two flavors:
- It is packaged as a stand-alone runnable jar (with all dependencies assembled) and can be used to send batch templated emails directly from the command line. 
- It is also available as a Maven dependency that can be added to a Java project as it provides an API for programmatically sending batch emails.

With Batch Email Sender, you can send emails to a large number of people either from the command line or by calling the relevant Java classes provided. The emails will all be modeled after a template in which you can specify as many parameters as needed. These parameters will be replaced by the corresponding values for those headers in a CSV data file. 
A common use of a replaceable parameter is to set the recipient of each email based on a header in the CSV file.

# Usage
## Installation
JDK 1.8 is required at a minimum.

There are two distributions: a stand-alone jar and a Maven library.

To package the project as a single stand-alone jar:

```console
mvn clean compile assembly:single -Pcli
```

For those who do not want to build it manually, batch-email-sender-cli.jar can be downloaded from any of the [releases](https://github.com/LieutenantPeacock/batch-email-sender/releases) for direct use.

This project is also available as a [Maven artifact](https://search.maven.org/artifact/com.lt-peacock/batch-email-sender/1.1.2/jar). Maven users can include the library with the following dependency in pom.xml:

```xml
<dependency>
  <groupId>com.lt-peacock</groupId>
  <artifactId>batch-email-sender</artifactId>
  <version>1.1.2</version>
</dependency>
```

## Running from the Command Line

To run the project:

```console
java -jar -Dconfig=pathto\Config.properties -Ddata=pathto\Data.csv -Dtemplate=pathto\Template.txt pathtojar\batch-email-sender.jar
```

The example command above is for Windows. On Unix, forward slashes should be used instead of backslashes. (Forward slashes also work for Windows.)

### Parameters
(Set these using `-DparamName=paramValue`)

<table>
	<tr><th>Parameter Name</th><th>Description</th><th>Default Value</th></tr>
	<tr>
		<td>config</td><td>This parameter specifies the path for the properties file containing the configuration information (the port, host, username, password, and whether or not to use TLS instead of SSL). See <a href="#config-format">Config Format</a>.</td>
		<td>Config.properties under the directory where the command is run</td>
	</tr>
	<tr>
		<td>data</td><td>This parameter specifies the absolute path for the CSV data file. The first line of the CSV is interpreted as the header. See <a href="#data-format">Data Format</a>.</td>
		<td>Data.csv under the directory where the command is run</td>
	</tr>
	<tr>
		<td>template</td><td>This parameter specifies the absolute path for the text file containing the template email (with template parameters to be replaced with the values on each row of the data file enclosed with <code>${}</code>). See <a href="#template-format">Template Format</a>.</td>
		<td>Template.txt under the directory where the command is run</td>
	</tr>
	<tr>
		<td>start</td><td>This parameter specifies the record to start from.</td>
		<td><code>1</code>, i.e. the first record</td>
	</tr>
	<tr>
		<td>end</td><td>This parameter specifies the record to end at. If this value is larger than the number of records, it is taken to mean the last record.</td>
		<td>The last record (equivalent to <code>Integer.MAX_VALUE</code>)</td>
	</tr>
	<tr>
		<td>dryRun</td><td>This parameter specifies whether to initiate a dry run (without actually sending any emails); this is a boolean parameter, so no value is required.</td>
		<td><code>false</code></td>
	</tr>
</table>

### Specifying Parameters Via Properties File
There is an optional first argument to indicate the path to the properties file that specifies parameters. Command line parameters take precedence over these.

```console
java -jar pathtojar\batch-email-sender.jar pathtoproperties\Run.properties
```

### Template Format
The possible sections are TO, CC, BCC, SUBJECT, BODY, and ATTACHMENTS. All sections are optional. 

Each section must begin with a line containing the section name in uppercase enclosed in square brackets (`[]`), e.g. `[TO]`, `[CC]`. Each section must also end with a line containing only dashes (`-`) with at least three of them. The content of the section is in between the start and end lines. 

Replaceable parameters to be replaced using the CSV data consist of uppercase letters and underscores enclosed in `${}`, e.g. `${NAME}` would refer to the value of the NAME column in the current row of the CSV file. Note that one email is sent for each row in the CSV data file (by default). 

Single line comments start with `[--`, end with `--]` and must take up an entire line. These lines are ignored.

All emails sent by running the project from the command line use HTML format, so the template may contain all HTML elements supported in emails. The `<br>` element is automatically added to the end of every line except the last one so that lines still work in an intuitive manner.

A sample template file is shown below. See also: [doc/templates/EmailTemplate.txt](doc/templates/EmailTemplate.txt)

```
[TO]
${EMAIL}
--------
[CC]
someone@email.com
--------
[BCC]
--------
[SUBJECT]
Some Subject...
--------
[BODY]
Dear ${NAME},

This is some content.

<sub>This email was sent programmatically.</sub>

[-- This is a comment. --]
--------
[ATTACHMENTS]
path\to\Image.png
--------
```

### Config Format
The config file specifies the SMTP properties as well as the username and pasword. It is a properties file.

A sample config file is shown below for a Gmail account. See also: [doc/templates/Config.properties](doc/templates/Config.properties)

```
username=some@gmail.com
password=12345678
host=smtp.gmail.com
port=587
tls=true
```

## Data Format
The data file should be a CSV with the first row as headers. Each subsequent row contains the parameter values for one email. The replaceable parameters specified in the template file as `${HEADER_NAME}` will be replaced using these values for each email.

A sample data file is shown below:

```
NAME,EMAIL
John Doe,john.doe@email.com
Mary Doe,mary.joe@email.com
```

## Programmatic Usage

First, construct an instance of `MailSender`. This can be your own implementation or the provided `BasicMailSender`.

`BasicMailSender` requires a `MailServerInfo` with the host, port, username, and password, as well as a parameter indicating whether or not to use TLS. The class contains a builder that can be used for more convenient instantiation.

```java
MailSender mailSender = new BasicMailSender(MailServerInfo.builder()
						.withHost("smtp.gmail.com")
						.withPort("587")
						.withTls(true)
						.withUsername("some@gmail.com")
						.withPassword("12345678")
						.build());
```

For a dry run where no emails are actually sent, pass in `true` as the second argument to the `BasicMailSender` constructor.

Next, construct a `MailWorker` to send the batch templated emails.

```java
MailWorker mailWorker = new MailWorker(mailSender);
```

For a dry run, pass in `true` as the second argument to the `MailWorker` constructor.

Then, create a `EmailTemplate`. `EmailTemplate.builder()` can be used to build the `EmailTemplate` programmatically to set the List of email addresses to directly send to, 
 a List of email addresses to send carbon copies to, a List of email addresses to 
 send blind carbon copies to, the subject, a List of lines for the body, 
 and a List of file paths for attachments.

```java
EmailTemplate template = EmailTemplate.builder().withTo(Arrays.asList("a@gmail.com", "b@gmail.com"))
		.withBody(Arrays.asList("Line 1", "Line 2", "Line 3...")).build();
```

An `EmailTemplate` can also be created from an `InputStream` in the [correct format](#template-format) specified above.

```java
EmailTemplate template = EmailTemplateParser.parseTemplate(new FileInputStream("path/to/Template.txt"));
```

After, get an `InputStream` for the CSV data file. See [Data Format](#data-format).

```java
InputStream data = new FileInputStream("path/to/Data.csv");
```

Finally, call the `sendTemplatedEmails` method on the `MailWorker`. This will send one email for each row apart from the header in the CSV data.

```java
mailWorker.sendTemplatedEmails(data, template);
```

To specify the row to start from and end at (both of which are one-indexed), pass in two more arguments.

```java
int startIndex = 3, endIndex = 10;
mailWorker.sendTemplatedEmails(data, template, startIndex, endIndex);
```

The end index may be set to `Integer.MAX_VALUE` to indicate the end of the data.