package com.nowcoder.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueTests {
    public static void main(String[] args){
        BlockingQueue q = new ArrayBlockingQueue(10);
        new Thread(new Producer(q)).start();
        new Thread(new Consumer(q)).start();
        new Thread(new Consumer(q)).start();
        new Thread(new Consumer(q)).start();
    }

}
class Producer implements Runnable{
    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue){
        this.queue =queue;
    }
    @Override
    public void run(){
        try{
            for(int i = 0; i < 100; i++){
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + " produce: " + queue.size());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

class Consumer implements Runnable{
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue){
        this.queue =queue;
    }
    @Override
    public void run(){
        try{
            while(true){
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName() + " consume: " + queue.size());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
