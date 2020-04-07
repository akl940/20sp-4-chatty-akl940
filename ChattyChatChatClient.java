import java.io.*; 
import java.net.*; 
import java.util.Scanner; 

public class ChattyChatChatClient {

	public static void main(String args[]) throws UnknownHostException, IOException
	{
		Scanner sc = new Scanner(System.in);
		Socket socket = new Socket(args[0], Integer.parseInt(args[1]));
		
		DataInputStream input = new DataInputStream(socket.getInputStream());
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		
		SendClientMsg scm = new SendClientMsg(output, sc);
		ReadClientMsg rcm = new ReadClientMsg(input);
		
		Thread sendT = new Thread(scm);
		Thread readT = new Thread(rcm);
		
		sendT.start();
		readT.start();
	}
	
	public void stopIO(DataInputStream input, DataOutputStream output, Socket socket) //close resources 
    {
        try
        { 
            input.close();
            output.close();
            socket.close();
        }
        catch(IOException e)
        {  e.printStackTrace(); }    
    }
}

class SendClientMsg implements Runnable //send from client to server
{
	final DataOutputStream output;
	final Scanner sc;
	
	public SendClientMsg(DataOutputStream output, Scanner sc)
	{
		this.output = output;
		this.sc = sc;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			String msg = sc.nextLine();
			try
			{
				output.writeUTF(msg);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
}

class ReadClientMsg implements Runnable //read message sent to this client
{
	final DataInputStream input;
	
	public ReadClientMsg(DataInputStream input)
	{
		this.input = input;
	}
	
	@Override
	public void run()
	{
		try
		{
			String msg;
			while(true)
			{
				msg = input.readUTF(); //read message
				System.out.println(msg); //display message
			}
		}
		catch(IOException e)
		{
			System.exit(0);
		}
	}
}

