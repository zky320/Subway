**使用的命令行语句**

1. 获取对应的自定义地铁文件（命名为 subway.txt）

```
java -cp .:* subway -map subway.txt
```

2. 获取指定铁路线路的所有站点名称，并存入指定文件

```
java -cp .:* subway -a 1号线 -map subway.txt -o station.txt
```

输出文件格式：

```
1号线：
······
洪湖里
西站（可转6号线）
西北角
······
```

3. 获取起点站和终点站的最短路径，并存入指定文件

```
java -cp .:* subway -b 洪湖里 复兴路 -map subway.txt -o routine.txt
```

输出文件格式：

```
3        
洪湖里
西站
6号线
复兴路
```



Tips：java文件中需要调用jar包，命令行中的格式为 java -cp .:A.jar B ，而 java -cp .:* subway 可以调用当前文件夹中的所有jar包。将文件下载后放入1个文件夹中，后用使用命令行进行测试。