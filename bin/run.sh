classpath=.
for i in `ls ../lib/*.jar`;
    do classpath=$classpath:$i;
done
for i in `ls ../bin/*.jar`;
    do classpath=$classpath:$i;
done
LOG=-DLOG=$1
ARG1=$1
echo $LOG, ARG1=$ARG1
#echo $classpath
java -DSEARCH.home=../ $LOG -cp "${classpath}" org.langke.testscript.Test $ARG1 $2 $3