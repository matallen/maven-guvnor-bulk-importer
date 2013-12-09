maven-guvnor-bulk-importer
==========================

Drools Guvnor importer forked from the droolsjbpm 5.x branch

New features added since branching from droolsjbpm:
* 5.3.10000.BRMS - support for bpmn2 processes (knowledge bases and snapshots)
* 5.3.10000.BRMS - made into maven mojo
* 5.3.10000.BRMS - changed version into release version to prevent SNAPSHOT updates during build process
* 5.3.10001.BRMS - support for multiple model files directly from a maven repo using its GAV

Coming up:
* 5.3.10002.BRMS - support for global models and references from packages to reduce repo size

Usage:
======

	<plugin>
		<groupId>org.drools.maven</groupId>
		<artifactId>maven-guvnor-bulk-importer</artifactId>
		<version>5.3.10000.BRMS</version>
		<executions>
			<execution>
				<id>generate-brms-import</id>
				<phase>prepare-package</phase>
				<goals>
					<goal>generate</goal>
				</goals>
				<configuration>
					<debug>true</debug>
					<debugExtra>true</debugExtra>
					<recursive>true</recursive>
					<path>${basedir}/src/main/resources/rules</path>
					<packageExclude>[0-9|.]*[.|-]+[SNAPSHOT]+[.|-]*[09|.]*</packageExclude>
					<fileExtensions>drl,xls,bpmn</fileExtensions>
					<creator>admin</creator>
					<outputFile>${project.build.directory}/guvnor-import.xml</outputFile>
					<snapshotName>1.0.0-SNAPSHOT</snapshotName>
					
					<!-- will upload the specified model jar to every package 
					<models>
						<model>
							<groupId>org.jboss.super</groupId>
							<artifactId>business-rules-domain</artifactId>
							<version>1.0.0-SNAPSHOT</version>
						</model>						
					</models>
					-->
					<!-- will look for an include the following functions file in each rule package
					<functionFileName>functions.txt</functionFileName>
					-->
					<!-- will configure the kagent changeset file
					<kagentChangeSetServer>http://localhost:8080/org.drools.guvnor.Guvnor/package/</kagentChangeSetServer>
					<kagentChangeSetFile>kagent-changeset.xml</kagentChangeSetFile>
					-->
				</configuration>
			</execution>
		</executions>
	</plugin>

Versioning:
===========

I recommend versioning the import file in maven by including a plugin such as this (xml is not a valid archive so the type is jar):

	<plugin>
		<groupId>org.codehaus.mojo</groupId>
		<artifactId>build-helper-maven-plugin</artifactId>
		<executions>
			<execution>
				<id>attach-import-xml</id>
				<phase>package</phase>
				<goals>
					<goal>attach-artifact</goal>
				</goals>
				<configuration>
					<artifacts>
						<artifact>
							<file>${project.build.directory}/guvnor-import.xml</file>
							<classifier>upload</classifier>
							<type>jar</type>
						</artifact>
					</artifacts>
				</configuration>
			</execution>
		</executions>
	</plugin>

			
