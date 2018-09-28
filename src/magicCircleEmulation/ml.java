package magicCircleEmulation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class ml implements Runnable{
	private String []degree= {"000","045","090","180","225","270","315"};
	private boolean []led= {false,false,false,false,false,false,false};
	private int baudrate;
	private int databits;
	private int parity;
	private int stopbit;
	private String lastMessage;
	private ConcurrentLinkedQueue<String> sendQueue;
	private SerialPort sp;
	private mazeMech mm;
	private boolean connected;
	private boolean mazesim;
	
	public ml(int baudrate, int databits, int parity, int stopbit)
	{
        this.baudrate = baudrate;
        this.databits = databits;
        this.parity = parity;
        this.stopbit = stopbit;
        lastMessage=new String();
        //this.ready = ready;
        this.connected = false;
        this.sendQueue = new ConcurrentLinkedQueue<>();
        mm = new mazeMech();
	}
    void updateCommPort(SerialPort commPort) {
        this.sp = commPort;
    }
    public void setMazeSim(boolean doit)
    {
    	this.mazesim=doit;
    }
	public void run()
	{
		try 
		{
			this.connected=true;
			mm.setConnected(this.connected);
			while(sp==null)
			{
                System.out.println("serial port not connected");
                Thread.sleep(500);
			}
			startLoop();
		}catch(InterruptedException ex)
		{
			System.out.println("run interrupted");
		}
	}
	public void startLoop()
	{
        byte[] readbuffer = new byte[5000];
        byte[] writebuffer = new byte[5000];
        char[] messagebuffer;
        int len;
        if(sp!=null)
        {
    		Thread mmCom = null;
        	while(!Thread.interrupted() && this.connected) 
        	{

        		if( mmCom==null && mazesim == true )
        		{
        			mm.setmazeMech(mazesim);
        			mmCom = new Thread(mm);
        			mmCom.start();
        		}
        		else if(mazesim == true)
        		{
        			this.sendQueue = mm.getSendQueue();
        			while(!this.sendQueue.isEmpty())
        			{
        				String strtmp = this.sendQueue.poll();
        				sp.writeBytes(strtmp.getBytes(), strtmp.length());
        			}
        		}
                sp.setComPortParameters(baudrate,databits,parity,stopbit );
                sp.openPort();
        		sp.addDataListener(new SerialPortDataListener() {
        			
        			@Override
        			public void serialEvent(SerialPortEvent event) {
        				// TODO Auto-generated method stub
        			      if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
        			          return;
        			       InputStream newData = sp.getInputStream();
        			       try {
        					if(newData.available()>1) {
        					   try {
        						newData.read(readbuffer);
             			       String str=new String(readbuffer);
             			       str=str.trim();
            			       if(lastMessage != str)
            			       {
            			    	   lastMessage = str;
            			    	   if(str.startsWith("IDENTIFY", 0))
            			    	   {
            			    		   String mess="Maurer Lab Arduino: MC, " + new Date().getTime()+"\r\n";
            			    		   sp.writeBytes(mess.getBytes(), mess.length());
            			    	   }
            			    	   else if(str.startsWith("MOTOR_CHECK",0))
            			    	   {
            			    		   for(int j=0;j<degree.length;j++)
            			    		   {
            			    			   String mess="Motor " + degree[j]+"\r\n";
                			    		   sp.writeBytes(mess.getBytes(), mess.length());
            			    		   }
            			    	   }
            			    	   else if(str.startsWith("LED", 0))
            			    	   {
        			    			   String []tmpstr = str.split(" ");
        			    			   for (int i = 0; i < led.length; i++)
        			    			   {
        			    				   if(degree[i].contentEquals(tmpstr[2]))
        			    				   {
        			    					   if(led[i]==true)
        			    					   {
        			    						   led[i]=false;
                    			    			   String mess="LED " + degree[i] +" OFF\r\n";
                        			    		   sp.writeBytes(mess.getBytes(), mess.length());
        			    					   }
        			    					   else
        			    					   {
        			    						   led[i]=true;
                    			    			   String mess="LED " + degree[i] +" ON\r\n";
                        			    		   sp.writeBytes(mess.getBytes(), mess.length());
        			    					   }
        			    				   }
        			    			   }
        			    			   
            			    	   }
            			    	   else if(str.startsWith("REWARD", 0))
            			    	   {
        			    			   String []tmpstr = str.split(" ");
        			    			   for (int i = 0; i < led.length; i++)
        			    			   {
        			    				   if(degree[i].contentEquals(tmpstr[1]))
        			    				   {
        			    				
                    			    			   String mess="Dispensing Reward " + degree[i] +" degree "+ new Date().getTime()+"\r\n";
                        			    		   sp.writeBytes(mess.getBytes(), mess.length());
        			    				   }
        			    			   }
        			    			   
            			    	   }
            			    	   else
            			    	   {
            			    		   String mess="Unknow Command\r\n";
            			    		   sp.writeBytes(mess.getBytes(), mess.length());
            			    	   }
            			       }
        					} catch (IOException e) {
        						// TODO Auto-generated catch block
        						e.printStackTrace();
        					}
        					   }
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        			       
        			}
        			
        			@Override
        			public int getListeningEvents() {
        				// TODO Auto-generated method stub
        				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        			}
        		});
        	}
        }
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SerialPort []sps = SerialPort.getCommPorts();
		SerialPort sp = sps[1];

		ml mlds=new ml(9600,8,SerialPort.NO_PARITY,SerialPort.ONE_STOP_BIT);
		mlds.updateCommPort(sp);
		Thread commThread = new Thread(mlds);
		commThread.start();
		Scanner input = new Scanner(System.in);
		boolean quit=false;
		while(!quit)
		{
			if(input.hasNextLine())
			{
				String tmp = input.nextLine();
				if(tmp.toLowerCase().contentEquals("quit"))
				{
					quit=true;
					input.close();
				}
				else if(tmp.toLowerCase().contentEquals("maze on"))
				{
					mlds.setMazeSim(true);
				}
				else if(tmp.toLowerCase().contentEquals("maze off"))
				{
					mlds.setMazeSim(false);
				}
			}
		}
	}

}
