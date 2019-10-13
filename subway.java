import net.sf.json.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class subway {
    private static int num;  //地铁线路条数
    private static int trannum;  //转站数
    private static int flag=0;
    private static String[] subwayN; //装所有线路的线路名
    private static JSONObject all = new JSONObject();    //存放所有地铁信息
    private static int[][] graph;    //图
    private static int start;    //起点在图中位置
    private static int end;  //终点在图中位置
    private static int s_num;  //所在的是几号线（对应在subwayN数组中）
    private static int e_num;
    private static Map<String, Integer> record;  //记录图的各个点
    private static String[] rec; //记录图的各个点，与map结合可以轻松由站点名找到对应的图中位置，由图中位置轻松找到站点名
    private static String[] subwaytran_rec;  //记录每条地铁线路上包含的图中位置点

    public static void readtxt(String txt){
        int mark=0;
        int nn=0;   //用于添加subwayN，用于后期读取JsonArray来创建图
        int subwaynum=0;
        int subwaysum=0;
        JSONArray jArr = null;
        String txtname = txt;

        try (FileReader reader = new FileReader(txtname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] str=line.split(" ");
                Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                if(str.length==1&&mark==0){  //说明是第一行，记录地铁线路有几条
                    num=Integer.valueOf(str[0]);
                    subwayN=new String[num];    //将几号线的名字都存起来
                    mark++;
                }
                else if(str.length==1&&mark!=0){
                    trannum=Integer.valueOf(str[0]);
                }
                else if(pattern.matcher(str[0]).matches()&&pattern.matcher(str[1]).matches()){  //说明是描述线路的两个数字
                    subwaynum=Integer.valueOf(str[0]);
                    subwaysum=Integer.valueOf(str[1]);
                    subwayN[nn++]=str[0]+"号线";
                    jArr = new JSONArray();
                }
                else {  //正常站点名称
                    JSONObject jobj = new JSONObject();
                    try {
                        jobj.put("siteName", str[0]);
                        jobj.put("transfer", str[1]);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jArr.add(jobj);
                    flag++;
                    if(flag==subwaysum){
                        all.put(subwaynum+"号线",jArr);
                        flag=0;
                    }
                }
            }
            if(flag==num){
                all.put(subwaynum+"号线",jArr);
            }
        } catch (IOException e) {
            System.out.println("该文件不存在或有误");
        }
    }

    public static void writeallstation(String subwaynum,String txt) {
        if(!all.has(subwaynum)){
            System.out.println("不存在该号线路");
            return;
        }
        JSONArray Jarray = all.getJSONArray(subwaynum);
        try {
            File writeName = new File(txt); // 相对路径，如果没有则要建立一个新的output.txt文件
            writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(subwaynum+"：\n");
                for(int i=0;i<Jarray.size();i++){
                    // System.out.print(Jarray.getJSONObject(i).getString("siteName"));
                    out.write(Jarray.getJSONObject(i).getString("siteName"));
                    String num=Jarray.getJSONObject(i).getString("transfer");
                    if(!num.equals("0")){
                        if(num.length()==1){
                            // System.out.println("（可转"+num+"号线）");
                            out.write("（可转"+num+"号线）\n");
                        }
                        else{
                            // System.out.println("（可转"+num.charAt(0)+"号线和"+num.charAt(1)+"号线）");
                            out.write("（可转"+num.charAt(0)+"号线和"+num.charAt(1)+"号线）\n");
                        }
                    }
                    else{
                        // System.out.println();
                        out.write("\n");
                    }
                }
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stationsGraph(String site1,String site2) {
        int n=trannum+2;
        int g_i=0,g_j=0;
        graph=new int[n][n];    //将所有站点加入图中
        for(int i=0;i<n;i++){
            for(int j=0;j<n;j++){
                graph[i][j]=Integer.MAX_VALUE;
            }
        }
        int index=0;
        int mark=0;
        rec=new String[n];
        record = new HashMap<String, Integer>(); //用于记录各站点在图中的归属
        subwaytran_rec=new String[num];
        for(int i=0;i<num;i++){
            subwaytran_rec[i]=" ";
        }
        //形成转站点为结点的图
        for(int i=0;i<num;i++){
            mark=0;
            JSONArray array=all.getJSONArray(subwayN[i]);
            for(int j=0;j<array.size();j++){
                if(!array.getJSONObject(j).getString("transfer").equals("0")&&mark==0){    //一条线的第一个转站点
                    index=j;
                    mark++;
                    if(!record.containsKey(array.getJSONObject(j).getString("siteName"))&&subwayN[i].equals("1号线")){
                        subwaytran_rec[i]+=g_i+" ";
                        rec[g_i]=array.getJSONObject(j).getString("siteName");
                        record.put(array.getJSONObject(j).getString("siteName"),g_i);   //记录一下该站点在图中的归属
                    }
                    else if(!record.containsKey(array.getJSONObject(j).getString("siteName"))){
                        g_i++;
                        subwaytran_rec[i]+=g_i+" ";
                        rec[g_i]=array.getJSONObject(j).getString("siteName");
                        record.put(array.getJSONObject(j).getString("siteName"),g_i);   //记录一下该站点在图中的归属
                    }
                    else{
                        subwaytran_rec[i]+=record.get(array.getJSONObject(index).getString("siteName"))+" ";
                    }
                }
                else if(!array.getJSONObject(j).getString("transfer").equals("0")){   //一条线的其余转站点
                    //g_i 为上个站点的归属，g_i+1为该站点的归属
                    if(!record.containsKey(array.getJSONObject(j).getString("siteName"))){
                        g_i++;
                        subwaytran_rec[i]+=g_i+" ";
                        rec[g_i]=array.getJSONObject(j).getString("siteName");
                        record.put(array.getJSONObject(j).getString("siteName"),g_i);   //记录一下该站点在图中的归属
                        g_j=record.get(array.getJSONObject(index).getString("siteName"));
                        graph[g_i][g_j]=j-index;
                        graph[g_j][g_i]=j-index;
                        index=j;
                    }
                    else{   //以存在图中
                        g_j=record.get(array.getJSONObject(j).getString("siteName"));
                        int g_k=record.get(array.getJSONObject(index).getString("siteName"));
                        subwaytran_rec[i]+=g_j+" ";
                        if(graph[g_k][g_j]==Integer.MAX_VALUE){
                            graph[g_k][g_j]=j-index;
                            graph[g_j][g_k]=j-index;
                        }
                        index=j;
                    }
                }
            }
        }
//        记录起点和终点的主要信息
        int s_flag=0;   //是否需要插入的标志（需要插入为0）
        int e_flag=0;
        int station_s=0;    //地铁线路中的位置
        int station_e=0;
        for(int i=0;i<num;i++){
            JSONArray array=all.getJSONArray(subwayN[i]);
            for(int j=0;j<array.size();j++){
                if(array.getJSONObject(j).getString("siteName").equals(site1)){ //起点
                    s_num=i;
                    station_s=j;
                    if(!record.containsKey(array.getJSONObject(j).getString("siteName"))){
                        g_i++;
                        start=g_i;
                        subwaytran_rec[i]+=g_i+" ";
                        rec[g_i]=array.getJSONObject(j).getString("siteName");
                        record.put(array.getJSONObject(j).getString("siteName"),g_i);   //记录一下该站点在图中的归属
                    }
                    else{   //若起点为转站点之一，记下图中位置
                        s_flag++;
                        start=record.get(array.getJSONObject(j).getString("siteName"));
                    }
                }
                if(array.getJSONObject(j).getString("siteName").equals(site2)){ //终点
                    e_num=i;
                    station_e=j;
                    if(!record.containsKey(array.getJSONObject(j).getString("siteName"))){
                        g_i++;
                        end=g_i;
                        subwaytran_rec[i]+=g_i+" ";
                        rec[g_i]=array.getJSONObject(j).getString("siteName");
                        record.put(array.getJSONObject(j).getString("siteName"),g_i);   //记录一下该站点在图中的归属
                    }
                    else{   //若终点为转站点之一，记下图中位置
                        e_flag++;
                        end=record.get(array.getJSONObject(j).getString("siteName"));
                    }
                }
            }
        }
        //插入起点
        String s_before=null;
        String s_after=null;
        int s_distance_b=0;
        int s_distance_a=0;
        int s_mark=0;
        if(s_flag==0){
            JSONArray array=all.getJSONArray(subwayN[s_num]);
            for(int i=0;i<array.size();i++){
                if(!array.getJSONObject(i).getString("transfer").equals("0")) {
                    if(i<station_s){
                        s_before=array.getJSONObject(i).getString("siteName");
                        s_distance_b=station_s-i;
                    }
                    else if(i>station_s){
                        s_after=array.getJSONObject(i).getString("siteName");
                        s_distance_a=i-station_s;
                        break;
                    }
                    else{
                        //就是起点，不用管
                    }
                }
            }
            if(s_before!=null){
                g_j=record.get(s_before);
                graph[start][g_j]=s_distance_b;
                graph[g_j][start]=s_distance_b;
                s_mark++;
            }
            if(s_after!=null){
                g_j=record.get(s_after);
                graph[start][g_j]=s_distance_a;
                graph[g_j][start]=s_distance_a;
                s_mark++;
            }
            if(s_mark==2){
                graph[record.get(s_after)][record.get(s_before)]=Integer.MAX_VALUE;
                graph[record.get(s_before)][record.get(s_after)]=Integer.MAX_VALUE;
            }
        }
        //插入终点
        String e_before=null;
        String e_after=null;
        int e_distance_b=0;
        int e_distance_a=0;
        int e_mark=0;
        if(e_flag==0){
            JSONArray array=all.getJSONArray(subwayN[e_num]);
            for(int i=0;i<array.size();i++){
                if(!array.getJSONObject(i).getString("transfer").equals("0")) {
                    if(i<station_e){
                        e_before=array.getJSONObject(i).getString("siteName");
                        e_distance_b=station_e-i;
                    }
                    else if(i>station_e){
                        e_after=array.getJSONObject(i).getString("siteName");
                        e_distance_a=i-station_e;
                        break;
                    }
                    else{
                        //就是起点，不用管
                    }
                }
            }
            if(e_before!=null){
                g_j=record.get(e_before);
                graph[end][g_j]=e_distance_b;
                graph[g_j][end]=e_distance_b;
                e_mark++;
            }
            if(e_after!=null){
                g_j=record.get(e_after);
                graph[end][g_j]=e_distance_a;
                graph[g_j][end]=e_distance_a;
                e_mark++;
            }
            if(e_mark==2){
                graph[record.get(e_after)][record.get(e_before)]=Integer.MAX_VALUE;
                graph[record.get(e_before)][record.get(e_after)]=Integer.MAX_VALUE;
            }
        }
        if((s_after==e_after)&&(s_before==e_before)&&(s_after!=null&&s_before!=null)){   //起点和终点的前后转站点都相同，说明他们之间没有多余的点
            if(s_distance_b>e_distance_b){  //起点与前一个转站点的距离大于终点和前一个转站点的距离
                graph[start][record.get(s_before)]=Integer.MAX_VALUE;
                graph[record.get(s_before)][start]=Integer.MAX_VALUE;
            }
            else if(s_distance_b<e_distance_b){
                graph[end][record.get(e_before)]=Integer.MAX_VALUE;
                graph[record.get(e_before)][end]=Integer.MAX_VALUE;
            }
            if(s_distance_a>e_distance_a){  //起点与后一个转站点的距离大于终点和后一个转站点的距离
                graph[start][record.get(s_after)]=Integer.MAX_VALUE;
                graph[record.get(s_after)][start]=Integer.MAX_VALUE;
            }
            else if(s_distance_a<e_distance_a){
                graph[end][record.get(e_after)]=Integer.MAX_VALUE;
                graph[record.get(e_after)][end]=Integer.MAX_VALUE;
            }
            //把他们俩连在一起
            if(s_before==null){
                graph[end][start]=Math.abs(e_distance_a-s_distance_a);
                graph[start][end]=Math.abs(e_distance_a-s_distance_a);
            }
            else{
                graph[end][start]=Math.abs(e_distance_b-s_distance_b);
                graph[start][end]=Math.abs(e_distance_b-s_distance_b);
            }

        }
        for(int i=0;i<n;i++){
            graph[i][i]=0;
        }
    }

    public static String dijkstra(int start, int end) {
        // 初始化，第一个顶点求出
        int n=record.size();
        final int M = Integer.MAX_VALUE;
        int[] shortest = new int[n];  //存放从start到其他节点的最短路径
        boolean[] visited = new boolean[n]; //标记当前该顶点的最短路径是否已经求出，true表示已经求出
        shortest[start] = 0;
        visited[start] = true;

        //存放从start到其他各节点的最短路径
        String[] path = new String[n];
        for(int i = 0; i < n; i++){
            path[i] = new String(start + "->" + i);
        }
        for(int count = 0; count != n-1; count ++){
            //选出一个距离初始顶点最近的为标记顶点
            int k = M;
            int min = M;
            for(int i =0; i< n ; i++){//遍历每一个顶点
                if( !visited[i] && graph[start][i] != M){ //如果该顶点未被遍历过且与start相连
                    if(min == -1 || min > graph[start][i]){ //找到与start最近的点
                        min = graph[start][i];
                        k = i;
                    }
                }
            }
            //正确的图生成的矩阵不可能出现K== M的情况
            if(k == M) {
                System.out.println("the input map matrix is wrong!");
                return null;
            }
            shortest[k] = min;
            visited[k] = true;
            //以k为中心点，更新start到未访问点的距离
            for (int i = 0; i < n; i++) {
                if (!visited[i] && graph[k][i] != M) {
                    int callen = min + graph[k][i];
                    if (graph[start][i] == M || graph[start][i] > callen) {
                        graph[start][i] = callen;
                        path[i] = path[k] + "->" + i;
                    }
                }
            }
        }

        return path[end];
    }

    public static void writeshortest(String path,String txt) {
        String[] p=path.split("->");
        int sum=1;
        int mark=0; //标记
        int ff=0;
        int subway_num=0;
        int index_sub=-1;
        List<String> write=new ArrayList<String>();
        for(int i=1;i<p.length;i++){
            //查找一条同时包含p[i]和p[i-1]的地铁线路
            for(int j=0;j<num;j++){
                mark=0;
                String[] s=subwaytran_rec[j].split(" ");
                for(int k=1;k<s.length;k++){
//                    System.out.print(s[k]+" ");
                    if((s[k].equals(p[i]))||(s[k].equals(p[i-1]))){
                        mark++;
                        if(mark==2){
                            subway_num=j;
                            break;
                        }
                    }
                }
//                System.out.println();
                if(mark==2){
                    break;
                }
            }
            //找到后
            JSONArray array=all.getJSONArray(subwayN[subway_num]);
            int index=0;
            mark=0;
            if(ff==0){
                ff++;
            }
            else{
                if(index_sub!=subway_num){
                    write.add(subwayN[subway_num]);
                }
            }
            index_sub=subway_num;
            for(int j=0;j<array.size();j++){
                //输出p[i-1]到p[i]之间的所有站（不包括p[i-1]）
                String station=array.getJSONObject(j).getString("siteName");
                if(station.equals(rec[Integer.valueOf(p[i-1])])&&mark==0){   //找到p[i-1]
                    mark=1;    //正序继续
                    continue;
                }
                else if(station.equals(rec[Integer.valueOf(p[i])])&&mark==0){
                    mark=-1;    //需要倒序
                }
                else if(station.equals(rec[Integer.valueOf(p[i-1])])&&mark==-1){
                    index=j;
                    break;
                }
                //正序可完成则继续
                if(mark==1){
                    write.add(array.getJSONObject(j).getString("siteName"));
                    sum++;
                    if(station.equals(rec[Integer.valueOf(p[i])])){
                        break;
                    }
                }
            }
            if(mark==-1){
                for(int j=index-1;j>=0;j--){
                    String station=array.getJSONObject(j).getString("siteName");
                    if(station.equals(rec[Integer.valueOf(p[i])])){
                        write.add(array.getJSONObject(j).getString("siteName"));
                        sum++;
                        break;
                    }
                    write.add(array.getJSONObject(j).getString("siteName"));
                    sum++;
                }
            }
        }

        try {
            File writeName = new File(txt);
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)
            ) {
                out.write(sum+"\n"+rec[start]+'\n');
                for(int i=0;i<write.size();i++){
                    out.write(write.get(i)+'\n');
                }
                out.flush(); // 把缓存区内容压入文件
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String readtxtname;
        String writetxtname;
        String subwaynum;
        String site1;
        String site2;
        //-map subway.txt
        if(args[0].equals("-map")){
            try{
                readtxtname=args[1];
                readtxt(readtxtname);
            }
            catch(Exception e){
                System.out.println("操作有误");
            }
        }
        //-a 1号线 -map subway.txt -o station.txt
        else if(args[0].equals("-a")){  //查询指定地铁线路
            try{
                subwaynum=args[1];
                if(args[2].equals("-map")){
                    readtxtname=args[3];
                    readtxt(readtxtname);
                }
                else{
                    System.out.println("命令有误");
                }
                if(args[4].equals("-o")) {
                    writetxtname = args[5];
                    writeallstation(subwaynum,writetxtname);
                }
                else{
                    System.out.println("命令有误");
                }
            }
            catch(Exception e){
                System.out.println("操作有误");
            }
        }
        //b 洪湖里 复兴路 -map subway.txt -o routine.txt
        else if(args[0].equals("-b")){  //求最短路径
            try{
                site1=args[1];
                site2=args[2];
                if(site1.equals(site2)){
                    System.out.println("起点终点相同");
                    System.exit(0);
                }
                if(args[3].equals("-map")){
                    readtxtname=args[4];
                    readtxt(readtxtname);
                }
                else{
                    System.out.println("命令有误");
                }
                if(args[5].equals("-o")) {
                    writetxtname = args[6];
                    //生成相应的图
                    stationsGraph(site1,site2);
                    String path=dijkstra(start,end);
                    writeshortest(path,writetxtname);
                }
                else{
                    System.out.println("命令有误");
                }
            }
            catch(Exception e){
                System.out.println("操作有误");
            }
        }
        else {
            System.out.println("找不到" + args[0] + "操作");
        }

    }

}
