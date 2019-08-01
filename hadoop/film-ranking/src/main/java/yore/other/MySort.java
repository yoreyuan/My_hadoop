package yore.other;

import java.util.Arrays;

/**
 * Created by yore on 2018/7/30 10:51
 */
public class MySort {

    public static void main(String[] args) {
        int[] arr = new int[]{
                6, 1, 2, 7, 9, 3, 4, 5, 10, 8
//                10,7,2,4,7,62,3,4,2,1,8,9,19
        };

        System.out.println("source：" + Arrays.toString(arr));

//        bubbleSort(arr);
        selectSort(arr);

//        quickSort(arr, 0, arr.length-1);
        MergeSort.sort(arr, 0, arr.length-1);

        System.out.println("result：" + Arrays.toString(arr));

    }

    //TODO 插入排序


    /**
     * 合并排序（归并排序）
     * 有点：稳定的排序排序算法
     */
    static class MergeSort {
        public static int[] sort(int[] a,int low,int high){
            int mid = (low+high)/2;  //划分子序列
            if(low<high){
                sort(a,low,mid);    //对左侧子序列进行递归排序
                sort(a,mid+1,high); //对右侧子序列进行递归排序
                merge(a,low,mid,high);  //左右归并
            }
            return a;
        }

        public static void merge(int[] a, int low, int mid, int high) {
            int[] temp = new int[high-low+1];   //辅助数组
            int i= low,j = mid+1, k=0;
            // 把较小的数先移到新数组中
            while(i<=mid && j<=high){
                if(a[i]<a[j]){
                    temp[k++] = a[i++];
                }else{
                    temp[k++] = a[j++];
                }
            }
            // 把左边剩余的数移入数组
            while(i<=mid) temp[k++] = a[i++];

            // 把右边边剩余的数移入数组
            while(j<=high) temp[k++] = a[j++];

            // 把新数组中的数覆盖nums数组
            for(int x=0;x<temp.length;x++) a[x+low] = temp[x];
        }

    }


    /**
     * 快速排序。思想分而治之（分治法）
     *
     * @param arr 待排序的数组
     * @param sentry_i 哨兵i的位置
     * @param sentry_j 哨兵j的位置
     * @date: 2018/7/30 11:20 AM
     */
    public static void quickSort(int[] arr, int sentry_i, int sentry_j){
//        System.out.println("flag = " + flag);

        int i, j, temp, t;
        if(sentry_i > sentry_j)return;
        System.out.println(Arrays.toString(arr));

        i = sentry_i;
        j = sentry_j;
        // 基准值，以哨兵i处的值为基准
        temp = arr[sentry_i];

        while (i < j){
            // 右边哨兵先行，寻找比基准数大的
            while (temp<=arr[j] && i<j)j--;

            // 再左边哨兵，寻找比基准数小的
            while (temp>=arr[i] && i<j)i++;


            // 满足上面条件之后，进行交换
            if(i<j){
                t = arr[j];
                arr[j] = arr[i];
                arr[i] = t;
            }
        }

        //基准值和i/j处的值进行交换
        arr[sentry_i] = arr[i];
        arr[i] = temp;

        // 递归左半侧
        quickSort(arr, sentry_i, j-1);
        // 递归右半侧
//        System.out.println(sentry_j);
        quickSort(arr, j+1, sentry_j);
    }


    /**
     * 冒泡排序
     *
     * @param arr 待排序的数组
     * @date: 2016/4/30 3:37 PM
     */
    public static void bubbleSort(int[] arr){
        for(int j=0;j<arr.length;j++){
            for (int i = 0; i <  arr.length - 1 -j ; i++) {
                if (arr[i] > arr[i + 1]) {
                    //值交换
                    int t = arr[i];
                    arr[i] = arr[i + 1];
                    arr[i + 1] = t;
                }
            }
        }
    }

    /**
     * 选择排序
     *
     * @param arr 待排序的数组
     * @date: 2016/4/30 3:45 PM
     */
    public static void selectSort(int[] arr){
        for (int index = 0; index < arr.length - 1; index++) {
            for (int i = index + 1; i < arr.length; i++) {
                if (arr[index] > arr[i]) {
                    int t = arr[index];
                    arr[index] = arr[i];
                    arr[i] = t;
                }
            }
        }
    }


}
