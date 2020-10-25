
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AntColonyOptimization_Test {


    private static int[] task;//任务数组,数组下标为任务id，数组内数据为任务长度
    private static int taskNum;//任务数量
    private static int[] node;//处理节点数组，下标为id，数据为处理能力
    private static int nodeNum;//处理节点数量

    private static int iteratorNum;//迭代次数
    private static int AntNumber;//蚂蚁数量，🐜；
    private static double[][] timeMatrix;//处理时间矩阵，第一个下标为任务id，第二个下标为节点id，数据为任务执行时间；

    private static double[][] pheromoneMatrix;//信息素矩阵，记录任务i给节点j的信息素浓度，
    private static int[] MaxpheromoneMatrix;//信息素矩阵中第i行最大信息素浓度的下标；比如m【0】=5，就是第0行的信息素浓度最高的下标为5；
    private static int[] criticalPointMatrix;//在第i次迭代中，采用随机分配策略的蚂蚁的临界编号，比如c【0】=5，代表从6开始之后的蚂蚁，都是随机分配，0-5号蚂
    //蚁按信息素分配

    private static double p = 0.8;//完成一次迭代后的信息素衰减比例
    private static double q = 1.2;//蚂蚁经过一条路径后信息素增加的比例；

    private static List<int[]> resultData = new ArrayList<>();
    //static List<int[][]> pathMatrix_allAnt;

    public static void main(String[] args) {
        //设置迭代次数
        iteratorNum = 20;
        //设置蚂蚁数量
        AntNumber = 100;
        //设置任务数量
        taskNum = 100;
        //设置节点数量
        nodeNum = 10;
        //初始化矩阵
        timeMatrix = new double[taskNum][nodeNum];
        pheromoneMatrix = new double[taskNum][nodeNum] ;
        MaxpheromoneMatrix = new int[taskNum];
        criticalPointMatrix = new int[taskNum];

        //初始化任务矩阵
        task = initTaskArray(taskNum);
        //初始化节点矩阵
        node = initNodeArray(nodeNum);
        //初始化时间矩阵
        initTimeMatrix(task,node);
        //初始化信息素矩阵
        initPheromoneMatrix(taskNum,nodeNum);

        //迭代搜索
        acoSearch(iteratorNum,AntNumber);

        for (int[] it : resultData)
        {
            for(int i = 0;i<it.length;i++)
                System.out.print(it[i]+" ");
            System.out.println();
        }
        int minTime = 65535;
        int minIndex = 0;
        for (int[] it : resultData)
        {
            for(int i = 0;i<it.length;i++)
                if(it[i]<minTime)
                {
                    minTime = it[i];
                    minIndex = i;
                }
        }
        logln("所以最佳方案时间为："+minTime+"最佳分配方案编号为："+minIndex+"最佳分配方案如下：");

        for(int i = 0 ;i<taskNum;i++)
        {
            logln("任务"+i+"分配给"+MaxpheromoneMatrix[i]+"节点");
        }
        /*for (int i = 0;i<taskNum;i++)
        {

            for (int j = 0;j<nodeNum;j++)
            {
                if(pathMatrix_allAnt.get(minIndex)[i][j] == 1)
                {
                    logln("任务"+i+"分配给节点"+j);
                }
            }
            //logln("");
        }*/

    }

    private static void logln(String s)
    {
        System.out.println(s);
    }
    private static void log(String s)
    {
        System.out.print(s);
    }

    /**
     * 迭代搜索
     * @param iteratorNum 迭代次数
     * @param antNumber 蚂蚁数量
     */

    private static void acoSearch(int iteratorNum, int antNumber) {
        //开始迭代
        logln("开始迭代。。。。");
        for (int i = 0;i<iteratorNum;i++)
        {
            logln("正在进行第"+i+"次迭代....");
            //记录此次迭代中所有蚂蚁的路径
            List<int[][]> pathMatrix_allAnt = new ArrayList<>();

            for (int anti = 0;anti<antNumber;anti++)
            {
                //第anti只蚂蚁的分配策略，pathmatrix[i][j]表示第i只蚂蚁将任务i给j
                //处理
                int[][] pathMatrix_oneAnt = initMatrix(taskNum,nodeNum,0);//这里是一只蚂蚁的路径，先将所有任务i到j的路径置为0
                logln("第"+anti+"只蚂蚁的路径矩阵：");
                for(int taski = 0;taski<taskNum;taski++)
                {
                    //将第taski个任务分配给第nodecount个节点处理；
                    int nodeCount = assignOneTask(anti,taski,node,pheromoneMatrix);//通过信息素，将任务分配给nodecpunt，由于critical初始值为0，所以第一次迭代时蚂蚁0总是按信息素把所有任务分给0节点
                    //第一次迭代时第二只蚂蚁往后都是随机分配节点
                    pathMatrix_oneAnt[taski][nodeCount] = 1;//将这个路径记录下来，就是把矩阵中对应点的值变为1
                }
                for(int i1 = 0;i1<taskNum;i1++)
                {
                    for (int j = 0;j<nodeNum;j++)
                    {
                        log(pathMatrix_oneAnt[i1][j]+" ");
                    }
                    logln("");
                }
                //将当前蚂蚁的路径加入所有蚂蚁的路径集合中
                pathMatrix_allAnt.add(pathMatrix_oneAnt);//所有蚂蚁的路径全部都记录下来
            }
            //计算每一次分配完任务后的处理时间
            int[] timeArray_oneIt = CalTimeoneIt(pathMatrix_allAnt);
            log("本次每个蚂蚁完成任务的时间为：");
            for (int i1 = 0; i1<antNumber;i1++)
                log(timeArray_oneIt[i1]+" ");
            logln("");
            //把每一次的结果记录下来，主要是时间结果
            //这里记录的是本次迭代，每个蚂蚁完成所有任务的分别用时是多少
            resultData.add(timeArray_oneIt);

            //更新信息素矩阵
            updatePheromoneMatrix(pathMatrix_allAnt,pheromoneMatrix,timeArray_oneIt);

        }

    }
    //更新信息素矩阵
    private static void updatePheromoneMatrix(List<int[][]> pathMatrix_allAnt, double[][] pheromoneMatrix, int[] timeArray_oneIt) {

        logln("信息素衰减.....");
        //第一次运行，信息素矩阵的浓度为1，所以乘以信息素浓度系数p，等于将所有信息素浓度衰减为0.5；以后每次都衰减为之前的0.5倍
        logln("衰减后的信息素矩阵为：");
        for (int i = 0;i<taskNum;i++)
        {
            for (int j = 0;j<nodeNum;j++)
            {
                pheromoneMatrix[i][j] *= p;
                log(pheromoneMatrix[i][j]+" ");
            }
            logln("");
        }
        //设置最小时间为65535
        int minTime = 65535;
        //最小的下标蚂蚁为-1
        int minIndex = -1;
        //对每一只蚂蚁，找到完成这个任务的最小时间，将最小的下标设置为这个蚂蚁的下标
        for (var antIndex = 0;antIndex<AntNumber;antIndex++)
        {
            if(timeArray_oneIt[antIndex]<minTime)
            {
                minTime = timeArray_oneIt[antIndex];
                minIndex = antIndex;
            }
        }
        logln("取得最小时间路径的蚂蚁为"+minIndex+"号蚂蚁，最小时间为"+minTime);
        logln("\n开始更新信息素......");
        //取得这个蚂蚁在完成这个任务时的路径，并且将这个路径的信息素乘以q加到这个路径的信息素上；
        for (int taskIndex = 0; taskIndex<taskNum; taskIndex++)
        {
            for(int nodeIndex = 0;nodeIndex<nodeNum;nodeIndex++)
            {
                if(pathMatrix_allAnt.get(minIndex)[taskIndex][nodeIndex]==1)
                {
                        pheromoneMatrix[taskIndex][nodeIndex] *=q;
                    //pheromoneMatrix[taskIndex][nodeIndex] += pheromoneMatrix[taskIndex][nodeIndex] *q;

                }
            }
        }
        logln("更新后的信息素矩阵为：");
        for (int i = 0;i<taskNum;i++)
        {
            for (int j = 0;j<nodeNum;j++)
            {
                pheromoneMatrix[i][j] *= p;
                log(pheromoneMatrix[i][j]+" ");
            }
            logln("");
        }
        logln("");

        //int[] maxPheromoneMatrix = new int[taskNum];//创建最大信息素矩阵
        //int[] criticalPointMatrix = new int[taskNum];//创建临界点分配矩阵


        for (int taskIndex= 0; taskIndex<taskNum;taskIndex++)
        {
            //取得信息素矩阵中这个任务在第0个节点运行的信息素，并设为最大信息素
            var maxPheromone = pheromoneMatrix[taskIndex][0];
            //设置最大信息素下标为0；即在第taskindex行第0列节点的信息素最大
            var maxIndex = 0;
            //设置信息素总和的初始值
            var sumPheromone = pheromoneMatrix[taskIndex][0];
            //默认信息素全相同
            var isAllSame = true;
            //对于每个节点，进行下面循环
            for (var nodeIndex = 1;nodeIndex<nodeNum;nodeIndex++)
            {
                //当这个节点的信息素，大于最大信息素的值，就把最大信息素的值设为这个值，并且把下标设为这个节点的下标
                if(pheromoneMatrix[taskIndex][nodeIndex]>maxPheromone){
                    maxPheromone = pheromoneMatrix[taskIndex][nodeIndex];
                    maxIndex = nodeIndex;
                }
                //当这个节点的信息素不等于前一个节点的信息素，就把全为相同的布尔值设为false；
                if(pheromoneMatrix[taskIndex][nodeIndex]!=pheromoneMatrix[taskIndex][nodeIndex-1]){
                    isAllSame = false;
                }
                //信息素总和加上这个节点的信息素
                sumPheromone += pheromoneMatrix[taskIndex][nodeIndex];
            }

            if(isAllSame)//如果这个任务的信息素浓度全相同，就随便选取一个节点作为最大信息素节点，最大信息素浓度为任意一点的信息素浓度
            {
                maxIndex = new Random().nextInt(nodeNum);
                maxPheromone = pheromoneMatrix[taskIndex][maxIndex];
            }
            logln("最大信息素为："+maxPheromone+"最大信息素节点为"+maxIndex);
            //这个任务的最大信息素节点被存储到最大信息素矩阵中，表示第taskIndex个任务的最大信息素节点为maxIndex；
            MaxpheromoneMatrix[taskIndex] = maxIndex;
            //这个任务的临界点蚂蚁数量为四舍五入取蚂蚁数量乘以最大信息素浓度除以总信息素浓度的值；
            logln("任务"+taskIndex+"的蚂蚁分配临界值之前为"+criticalPointMatrix[taskIndex]);
            criticalPointMatrix[taskIndex] = (int) Math.round(AntNumber*(maxPheromone/sumPheromone));
            logln("任务"+taskIndex+"的蚂蚁分配临界值现在为"+criticalPointMatrix[taskIndex]);
        }
    }

    /**
     * 计算一次迭代中，所有蚂蚁的处理任务时间
     * @param pathMatrix_allAnt 所有蚂蚁的路径
     * @return
     */
    private static int[] CalTimeoneIt(List<int[][]> pathMatrix_allAnt) {
        int[] time_allAnt = new int[AntNumber];
        for (int i = 0;i<pathMatrix_allAnt.size();i++)
        {
            var pathMatrix = pathMatrix_allAnt.get(i);//获取第i个蚂蚁的路径矩阵

            int maxTime = -1;//最大时间为-1
            for (int nodeIndex = 0;nodeIndex<nodeNum;nodeIndex++)
            {
                var time = 0;
                for (int taskIndex = 0;taskIndex<taskNum;taskIndex++)
                {
                    if(pathMatrix[taskIndex][nodeIndex] == 1)//如果任务没有在这个节点上运行，则就不会有时间，那就是最大时间为0
                    {
                        time += timeMatrix[taskIndex][nodeIndex];//时间矩阵存储了所有任务在每个节点上的运行时间
                    }
                }
                if(time>maxTime)
                    maxTime = time;//这个最大时间是指所有任务在节点nodeindex上运行的时间
            }
            time_allAnt[i] = maxTime;//最后计算这个蚂蚁所选的路径的最大时间为maxtime
            //得到的数组是这次迭代，所有蚂蚁完成任务的最大时间为多少
        }

        return time_allAnt;
    }

    /**
     * 将第taski个任务分配给某一节点处理
     * @param anti 蚂蚁编号
     * @param taski 任务编号
     * @param node 节点集合
     * @param pheromoneMatrix 信息素集合
     * @return
     */
    private static int assignOneTask(int anti, int taski, int[] node, double[][] pheromoneMatrix) {
        if(anti <= criticalPointMatrix[taski])//若蚂蚁编号在临界点前，则采用最大信息素方式
        {
            return MaxpheromoneMatrix[taski];
        }
        //否则就随机分配节点
        return new Random().nextInt(nodeNum-1);
    }

    private static int[][] initMatrix(int taskNum, int nodeNum, int num) {
        int [][] matrix = new int[taskNum][nodeNum];
        for (int i = 0;i<taskNum;i++)
        {
            for (int j = 0;j<nodeNum;j++)
            {
                matrix[i][j] = num;
            }
        }
        return matrix;
    }


    //将信息素浓度置1；
    private static void initPheromoneMatrix(int taskNum, int nodeNum) {
        //初始化信息素矩阵，并且对每一个元素的信息素浓度置为1；
        for (int i =0;i<taskNum;i++)
        {
            for (int j = 0;j<nodeNum;j++)
            {
                pheromoneMatrix[i][j] =1;
            }
        }

    }

    //计算节点处理时间
    private static void initTimeMatrix(int[] task, int[] node) {

        for (int i =0;i<task.length;i++)
        {
            for(int j=0;j<node.length;j++)
            {
                timeMatrix[i][j] = task[i]/node[j];
            }
        }
    }
    //初始化任务列表
    private static int[] initTaskArray(int taskNum)
    {
        int[] tk = new int[taskNum];
        for (int i =0;i<taskNum;i++)
        {
            tk[i] = new Random().nextInt(90)+10;

        }


        return tk;
    }

    private static int[] initNodeArray(int nodeNum)
    {
        int[] nd = new int[nodeNum];
        for (int i =0;i<nodeNum;i++)
        {
            nd[i] = new Random().nextInt(30)+10;
        }
        return nd;
    }



}