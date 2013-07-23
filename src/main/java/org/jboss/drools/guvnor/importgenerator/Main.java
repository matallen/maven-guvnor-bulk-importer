package org.jboss.drools.guvnor.importgenerator;

public class Main {
  public static void main(String[] x) throws Exception{
    CmdArgsParser a=new CmdArgsParser();
    String[] args=new String[]{ //default arguments
    "-classpath",
//    "-p", "/home/mallen/Work/svn/20130123-drools-guvnor-5.4.x[github]/guvnor-5.4.x/guvnor-bulk-importer/rules",
    "-p", "/home/mallen/Work/poc/maven-guvnor-bulk-importer/rules1",
    "-s", "rules1",
    "-e", "[0-9|.]*[.|-]+[SNAPSHOT]+[.|-]*[09|.]*",
    "-r", "true",
    "-u","admin",
    "-f","drl,xls,bpmn",
    "-o","generated.xml",
    "-n","1.0.0-SNAPSHOT",
//    "-c","functions.drl",
//    "-k", "http://localhost:8080/brms/org.drools.guvnor.Guvnor/package/",
//    "-b", "/home/mallen/Work/svn/20130123-drools-guvnor-5.4.x[github]/guvnor-5.4.x/guvnor-bulk-importer",
    "-b", "/home/mallen/Work/poc/maven-guvnor-bulk-importer",
//    "-w", "kagentChangeSet.xml",
    "-V"};
    a.parse(args);
    new ImportFileGenerator().run(a);
  }
}
