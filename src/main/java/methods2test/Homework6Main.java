package methods2test;

public class Homework6Main {
    public static void main(String[] args) {
        System.out.println("Домашняя работа №6");

        Homework6Main homework6Main = new Homework6Main();

        int[] arr = homework6Main.getElementsAfterLast4(new int[]{4, 2, 2, 0, 2, 3, 0, 1, 2});
        for (int i : arr) {
            System.out.print(i);
        }
        System.out.println();
        System.out.println(arr.length);

        System.out.println(homework6Main.checkArrayOn1And4(new int[]{4, 1, 2, 1, 3, 4}));


    }


    //Метод возвратит true, если в массиве есть и единицы и четверки (хотя бы в одном экземпляре):
    public int[] getElementsAfterLast4(int[] intArray) {
        int indexOfLast4 = -1;
        //Ищем позицию последней "4":
        for (int i = 0; i < intArray.length; i++) {
            if (intArray[i] == 4) {
                indexOfLast4 = i;
            }
        }
        if (indexOfLast4 == -1) { //если нет ни одной четверки, то кидаем исключение
            throw new RuntimeException("В массиве нет ни одной четверки!");
        } else {
            //Формируем результирующий массив:
            int[] resultArray = new int[intArray.length - indexOfLast4 - 1];
            for (int i = 0; i < intArray.length - indexOfLast4 - 1; i++) {
                resultArray[i] = intArray[i + indexOfLast4 + 1];
            }
            return resultArray;
        }
    }

    public boolean checkArrayOn1And4(int[] intArray) {
        boolean has1 = false;
        boolean has4 = false;

        for (int n : intArray) {
            if (!has1) {
                if (n == 1) {
                    has1 = true;
                }
            }
            if (!has4) {
                if (n == 4) {
                    has4 = true;
                }
            }
            if (has1 && has4) break;
        }
        return (has1 && has4);
    }
}
