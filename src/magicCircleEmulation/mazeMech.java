package magicCircleEmulation;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class mazeMech implements Runnable{

	private boolean []sensor= {false,false,false,false,false,false,false,false};
	private String []degree= {"000","045","090","135","180","225","270","315"};
	Random rand = new Random(System.currentTimeMillis());
	private boolean mazeMechOn;
	private boolean connected;
    ConcurrentLinkedQueue<String>sendQueue;
    public ConcurrentLinkedQueue<String> getSendQueue()
    {
    	return sendQueue;
    }
    public void setSendQueue(ConcurrentLinkedQueue<String> squeue)
    {
    	this.sendQueue=squeue;
    }
    public void setmazeMech(boolean co)
    {
    	this.mazeMechOn=co;
    }
    public void setConnected(boolean co)
    {
    	this.connected=co;
    }
	public void run()
	{
		while(!Thread.interrupted())
		{
			if(mazeMechOn == true && connected == true)
			{
				for(int i=0;i<rand.nextInt(100);i++)
				{
					sendQueue.add(degree[rand.nextInt(8)] + " Broken " + new Date().getTime() +"\r\n");
					try {
						Thread.sleep(rand.nextInt(1000));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void interruptThread()
	{
		Thread.currentThread().interrupt();
	}
	public mazeMech()
	{
		this.sendQueue = new ConcurrentLinkedQueue<>();
		mazeMechOn = false;
	}
}
