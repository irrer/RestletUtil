
Restlet related utilities.


The following shell commands were used to make the localhost certificate and java keystore (*.jks) file: 

	export keystore=keystore.jks
	export storepass=<fake_password>    # substitute real password
	
	rm -f ${keystore}
	
	keytool -genkey -v -alias TMQA_localhost -dname "CN=localhost, OU=Radiation Oncology, O=University of Michigan, C=US" \
        -keypass ${storepass} -keystore ${keystore} -storepass ${storepass} -keyalg "RSA" -sigalg "SHA512withRSA" -keysize 2048 -validity 36500

	keytool -genkey -v -alias TMQA_irrer -dname "CN=141.214.125.68, OU=Radiation Oncology, O=University of Michigan, C=US" \
        -keypass ${storepass} -keystore ${keystore} -storepass ${storepass} -keyalg "RSA" -sigalg "SHA512withRSA" -keysize 2048 -validity 36500

	for certFile in rodicom11dev.txt rodicom11cet.txt rodicom11prod.txt; do
	    keytool -importcert -file ${certFile} -trustcacerts -noprompt -keystore ${keystore} -keypass ${storepass} -storepass ${storepass}
    done	
	
	keytool -list -keystore ${keystore} -storepass ${storepass}

The last command will print the certificate's fingerprint.  This can be verified by visiting the
web server (ie: https://rodicom11dev) and looking at the associated server certificate.  Some SHA1
fingerprints as of 10 Nov 2015:

	dev  : 3B:B6:4E:12:8D:81:D2:65:E5:89:C1:4B:E3:87:96:78:FC:4D:AC:69
	cet  : F3:8F:5A:96:98:55:58:B0:B7:2C:11:AA:60:AF:1D:8E:D5:88:AD:13
	prod : EA:24:6A:15:52:6A:B3:9A:A4:C1:4E:FD:FD:62:8E:8D:17:50:34:D5