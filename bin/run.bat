@setlocal enabledelayedexpansion
@set classpath=.
@for %%c in (..\lib\*.jar) do @set classpath=!classpath!;%%c
@for %%c in (..\bin\*.jar) do @set classpath=!classpath!;%%c

java -DSEARCH.home=../ -cp "%classpath%" org.langke.testscript.Test %*
