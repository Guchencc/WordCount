import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class WordCount {
    private String filename;
    private int charcount=0;
    private int wordcount=0;
    private int linecount=0;
    private int codeLinecount=0;
    private int blankLinecount=0;
    private int commentLinecount=0;
    private ArrayList<String> optlist=new ArrayList<>();
    private ArrayList<String> stoplist=new ArrayList<>();
    private ArrayList<String> filelist=new ArrayList<>();
    private String suffix;
    private String outFileName;
    private String stopFileName;

    public static void main(String[] args) throws Exception{
        WordCount wordCount=new WordCount();
        wordCount.CommandParser(args);
        wordCount.Count();
    }

    public void CommandParser(String[] args) { //命令行参数解析，
        for(int i=0;i<args.length;i++){
            args[i]=args[i].toLowerCase();
            if (args[i].equals("-c")||args[i].equals("-l")||args[i].equals("-w")||args[i].equals("-a")||args[i].equals("-s")) //将计数命令直接添加进oplist
                optlist.add(args[i]);
            else if (args[i].equals("-e")){   //如果输入的命令包含-e 则后面必定是停用词文件路径
                i++;
                if (args.length>i) {
                    if (args[i].equals("-o") || args[i].equals("\n")) {
                        System.out.println("未输入停用单词表文件名！");
                        return;
                    }
                }else  {
                    System.out.println("未输入停用单词表文件名！");
                    return;
                }
                stopFileName=args[i];
                optlist.add(args[--i]);
                readStopFile();
            }
            else if (args[i].equals("-o")){  //如果输入的命令包含-o 则后面必定是输出文件路径
                if(++i==args.length) {
                    System.out.println("未输入输出文件名！");
                    return;
                }
                outFileName=args[i];
                optlist.add(args[--i]);
            }
            else if (args[i].equals(outFileName)||args[i].equals(stopFileName));
            else if (optlist.contains("-s") && args[i].matches(".*\\*[.](txt|c|py|java|cpp)$")){    //根据正则表达式匹配输入的含有通配符的路径
                String root="";
                suffix=args[i].substring(args[i].lastIndexOf('.')+1,args[i].length());  //获取想要获取的文件后缀名
                root=args[i].replaceAll("\\*[.](txt|c|py|java|cpp)$","");   //获取想要查找含有通配符格式的文件根目录
                if (root.length()<1)        //如果没有指定目录则从当前目录开始查找
                    root=System.getProperty("user.dir");
                findAllFiles(root);//查找匹配通配符格式的所有文件
            }
            else if (!optlist.contains("-s"))   //如果输入的命令行中无-s  则输入文件名不包含通配符，而是具体的文件路径，直接添加进filelist中
                filelist.add(args[i]);
        }
    }


    public void Count() throws Exception {
        String str="";
        boolean isstop=false;
        for (int i=0;i<filelist.size();i++){  //遍历待统计文件数组，并根据相关名词定义进行计数
            String path=filelist.get(i);
            filename=path.substring(path.lastIndexOf('\\')+1,path.length());
            BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(filelist.get(i))));
            while((str=br.readLine())!=null) {
                if (str.trim().replaceAll("(//.*|/\\*.*\\*/|/\\*.*|\\*/|\\{|\\})","").length()>1)
                    codeLinecount++;
                if (str.trim().matches("^(//|/\\*).*") || str.trim().matches("^[!-~](//.*|/\\*.*\\*/)") || str.trim().matches("\\*/")){
                    commentLinecount++;
                }
                if(str.matches("\\s*") || (str.trim().length()==1 && (str.trim().charAt(0)>0x21 && str.trim().charAt(0)<0x7F)))
                    blankLinecount++;

                linecount++;
                charcount+=str.length();
                String[] words=str.trim().split("\\s|,");
                if (optlist.contains("-e")){    //如果命令包含-e 则遍历单词，若遇到与stoplist中相同的单词则不进行word计数
                    for(String word:words) {
                        for (String stopword : stoplist) {
                            if (word.equals(stopword))
                                isstop = true;
                        }
                        if (!isstop && !word.equals(""))
                            wordcount++;
                        isstop = false;
                    }
                }
                else {
                    for (String word:words)
                        if (!word.equals(""))   //如果命令不包含-e 则直接对单词计数
                            wordcount++;
                }
                isstop=false;
            }
            charcount=charcount+linecount-1;    // \r\t

            if (optlist.contains("-c")){    //根据oplist中的统计命令选择输出结果
                System.out.println(filename+","+"字符数:"+charcount);
            }
            if (optlist.contains("-w")){
                System.out.println(filename+","+"单词数:"+wordcount);
            }
            if (optlist.contains("-l")){
                System.out.println(filename+","+"行数:"+linecount);
            }
            if (optlist.contains("-a")){
                System.out.println(filename+","+"代码行/空行/注释行:"+codeLinecount+"/"+blankLinecount+"/"+commentLinecount);
            }
            outprint();     //记录统计结果
            resetCount();   //重置计数器
        }
    }

    public void outprint(){
        File file=null;
        String str="";
        if (!optlist.contains("-c")&&!optlist.contains("-w")&&!optlist.contains("-l")&&!optlist.contains("-a")){
            System.out.println("无统计操作，无输出项！");
            return;
        }
        if(optlist.contains("-o") && outFileName!=null)
            file = new File(outFileName);       //如果指定输出文件路径，则在相应路径输出统计结果
        else
            file = new File("result.txt");      //否则在当前路径下的result.txt文件中输出。
        try{
            FileWriter fw=new FileWriter(file,true);
            PrintWriter pw=new PrintWriter(fw);
            if(!file.exists()){
                file.createNewFile();
            }
            if (optlist.contains("-c"))         //根据oplist中的命令选择记录统计结果
                str+=filename+","+"字符数:"+charcount+"\r\n";
            if (optlist.contains("-w"))
                str+=filename+","+"单词数:"+wordcount+"\r\n";
            if (optlist.contains("-l"))
                str+=filename+","+"行数:"+linecount+"\r\n";
            if (optlist.contains("-a"))
                str+=filename+","+"代码行/空行/注释行:"+codeLinecount+"/"+blankLinecount+"/"+commentLinecount+"\r\n";
            pw.write(str);
            pw.close();
            fw.close();
        }catch (Exception e){
            System.out.println("输出文件失败！");
        }
    }


    public void resetCount(){
        charcount=0;
        wordcount=0;
        linecount=0;
        codeLinecount=0;
        blankLinecount=0;
        commentLinecount=0;
    }

    public void readStopFile(){
        String str="";
        String[] stopwords;
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(stopFileName)));
            while ((str = bf.readLine()) != null) {     //遍历输入的停用词文件并登记停用词
                stopwords = str.trim().split("\\s");
                Collections.addAll(stoplist, stopwords);
            }
        }catch (Exception e){
            System.out.println("读取停用词表错误！");
        }
    }

    public void findAllFiles(String path){
        File file=new File(path);
        if (!file.isDirectory()) {
            String filename = file.getName();
            if (filename.substring(filename.lastIndexOf('.') + 1, filename.length()).equals(suffix))   //将文件后缀与提取的通配符后缀比较，若相同则将该文件路径加入filelist
                filelist.add(file.getAbsolutePath());

        } else if (file.isDirectory()){
            for (File f:file.listFiles())
                findAllFiles(f.getAbsolutePath());      //递归遍历文件夹与文件
        }
    }
}

//-s -a -w -c -l F:\codes\java\try\src\*.c -o output.txt
//wc.exe -s -a -w F:\codes\java\try\src\*.c
//注释