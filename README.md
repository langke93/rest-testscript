rest-testscript
===============

base rest testscript 这是一个基于java http connection 、httpclient写的测试框架，可用于并发测试、性能测试，测试驱动开发等场景 可以支持自增、随机、循环、并发等标签;

运行用例：<br/>
<pre>
  linux:<br/>
	cd /data/search/rest-testscript/bin
	sh run.sh hash query
	sh run.sh -c100 -t60 url
	win:
	cd D:\workspaces\rest-testscript\bin
	run.bat hash query
</pre>

###快速并发测试
类似webbench方式进行压力测试
 sh run.sh -c10000 -t60 http://localhost:9900/demo/test?a=b
 
 
###编写测试用例
<pre>
cd rest-testscript/bin
mkdir -p ../script/jetty/jsp
vi ../script/jetty/test.properties 
#
BASE_URL=http\://localhost
PORT=9009
INDEX_NAME=
#Tag
RANDINT=
SEQINT=
QUERYWD=
QUERYWD.PATH=
#Command
SLEEP=
FOREACH=
CONCURRENT=

vi ../script/jetty/jsp/index.txt 
${BASE_URL}:${PORT}/index.jsp

GET



${CONCURRENT.300_100}
                       
</pre>
执行结果
<img src="https://raw.github.com/langke93/rest-testscript/master/doc/img/example-rest.jpg" title="rest 测试用例"/>
	
	读取运行测试用例脚本  Usage: org.langke.testscript.Test <project> <operate> <scriptSubDir>
	project测试脚本项目目录名称，
	operate测试步骤目录名称，可以是all表示递归执行该目录下所有脚本
	scriptSubDir 子目录名 执行步骤目录下子目录脚本
	只执行.txt格式脚本,每个步骤目录下可以有子目录，程序会递归执行
	
标签配置在每个脚本项目根目录下test.properties文件里，在运行时：<br/>
 	简单标签会把脚本里标签替换成properties文件里key对应的值<br/>
 	COST_TIME=costTime	用于并发时取服务端返回执行时间的JSON串<br/>

生成器标签：
 	 SEQINT，自增整数 Usage:${SEQINT.0}	0表初始值<br/>
     RANDINT，随机整数 Usage:${RANDINT.0_1000}	0_1000表随机值范围<br/>
     QUERYWD，随机关键词 Usage:${QUERYWD.RAND}	也可指定随机关键词文件${QUERYWD.randkwd2.txt}或者properties文件配合QUERYWD.PATH=randkwd.txt配置关键词文件，如果没有则生成英文数字混合随机字符串<br/>
     RANDINT,QUERYWD有设置随机种子数，也就是每次运行的随机顺序的值会相同<br/>

指令标签，（配置在测试脚本第五段，如果没有第四段需要留出空行）：<br/>
		SLEEP，等待指令，Usage:${SLEEP.10}	10表等10秒,可以是浮点数 ，可用于schema创建时等待shard分配等等<br/>
		FOREACH,循环指令，Usage:${FOREACH.10} 10表循环10次<br/>
        CONCURRENT,并发指令Usage:${CONCURRENT.100_200} 100表并发数，200表任务执行个数， 并发结果汇报ResponseCode,ResponseMessage<br/>

测试脚本格式：
 		第一段URL，第二段METHOD，第三段请求BODY，第四段预计结果；第五段指令${FOREACH} ${SLEEP}<br/>
 		以空行区分每个段落,注意脚本格式:为空的段落需要留出空白段<br/>

 		