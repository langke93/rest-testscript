rest-testscript
===============

base rest testscript 这是一个基于java http connection写的测试框架，可用于并发测试、性能测试，测试驱动开发等场景 可以支持自增、随机、循环、并发等标签

运行用例：
  linux:
	cd /data/search/rest-testscript/bin
	sh run.sh hash query
	win:
	cd D:\workspaces\rest-testscript\bin
	run.bat hash query

	读取运行测试用例脚本  Usage: org.langke.testscript.Test <project> <operate> <scriptSubDir>
	project测试脚本项目目录名称，
	operate测试步骤目录名称，可以是all表示递归执行该目录下所有脚本
	scriptSubDir 子目录名 执行步骤目录下子目录脚本
	只执行.txt格式脚本,每个步骤目录下可以有子目录，程序会递归执行
标签配置在每个脚本项目根目录下test.properties文件里，在运行时：
 	简单标签会把脚本里标签替换成properties文件里key对应的值
 	COST_TIME=costTime	用于并发时取服务端返回执行时间的JSON串
 生成器标签：
 	 SEQINT，自增整数 Usage:${SEQINT.0}	0表初始值
     RANDINT，随机整数 Usage:${RANDINT.0_1000}	0_1000表随机值范围
     QUERYWD，随机关键词 Usage:${QUERYWD.RAND}	也可指定随机关键词文件${QUERYWD.randkwd2.txt}或者properties文件配合QUERYWD.PATH=randkwd.txt配置关键词文件，如果没有则生成英文数字混合随机字符串
     RANDINT,QUERYWD有设置随机种子数，也就是每次运行的随机顺序的值会相同
指令标签，（配置在测试脚本第五段，如果没有第四段需要留出空行）：
		SLEEP，等待指令，Usage:${SLEEP.10}	10表等10秒,可以是浮点数 ，可用于schema创建时等待shard分配等等
		FOREACH,循环指令，Usage:${FOREACH.10} 10表循环10次
        CONCURRENT,并发指令Usage:${CONCURRENT.100_200} 100表并发数，200表任务执行个数， 并发结果汇报ResponseCode,ResponseMessage
 测试脚本格式：
 		第一段URL，第二段METHOD，第三段请求BODY，第四段预计结果；第五段指令${FOREACH} ${SLEEP}
 		以空行区分每个段落,注意脚本格式:为空的段落需要留出空白段

 		aaa