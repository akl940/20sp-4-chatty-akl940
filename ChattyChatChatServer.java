import java.io.*;
import java.util.*;
import java.net.*;

public class ChattyChatChatServer {
	
	static Vector<ClientHandler> activeClientList = new Vector<>();
	
	public static void main(String[] args) throws IOException
	{
		ServerSocket ss = new ServerSocket(Integer.parseInt(args[0]));
		//^^listen on port # entered in
		
		Socket socket; //for clients to connect to
		
		while(true) //running server
		{
			socket = ss.accept(); //accept request
			
			DataInputStream input = new DataInputStream(socket.getInputStream());
			DataOutputStream output = new DataOutputStream(socket.getOutputStream());
			
			
			//for current client
			ClientHandler client = new ClientHandler(socket, input, output);
			Thread t = new Thread(client);
			activeClientList.add(client);
			t.start();
		}
	}
}

class ClientHandler implements Runnable //handles client input
{
	String name = "Anonymous"; //anonymous nickname unless/until user manually sets their own 
	final DataInputStream is;
	final DataOutputStream os;
	Socket socket;
	
	public ClientHandler(Socket socket, DataInputStream is, DataOutputStream os)
	{
		this.is = is;
		this.os = os;
		this.socket = socket;
	}
	
	@Override
	public void run()
	{
		String line;
		while(true) //listen for client input
		{
			try
			{
				line = is.readUTF();
				System.out.println(name + ": " + line);
				
				if(line.equals("/quit")) //user quits program
				{
					quitChat(); 
					break;
				}
				if(line.indexOf("/nick") >= 0)
					this.setNickname(parseMsg(line));
				else if(line.indexOf("/dm") >= 0)
					this.sendDM(parseMsg(line));
				else
					sendNormal(line);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public String[] parseMsg(String line)
	{
		String[] parsed = line.split(" ");
		return parsed;
	}
	
	//quit
	public void quitChat() //close IO
	{
		try
		{
			this.is.close();
			this.os.close();
			this.socket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//nick
	public void setNickname(String[] parsed)
	{
		this.name = parsed[1];
	}
	
	//dm
	public void sendDM(String[] parsed)
	{
		try
		{
			for (ClientHandler client : ChattyServer.activeClientList)
				dmHelper(client, parsed);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	public void dmHelper(ClientHandler client, String[] parsed) throws IOException
	{
		if(client.name.equals(parsed[1]))
		{
			parsed = actualMessageArray(parsed); //create new array to relay actual message without /dm and nickname
			String message = String.join(" ", parsed); //make into a single string
			client.os.writeUTF(this.name + ": " + message); //display message to client
		}
	}
	public String[] actualMessageArray(String[] array)
	{
		String[] newArray = new String[array.length-2];

        for(int i = 0; i < array.length-2; i++)
            newArray[i] = array[i+2]; //copy old to new String[]
            
        return newArray;
	}
	
	//no commands, just regular message to all
	public void sendNormal(String line)
	{
		try
		{
			for (ClientHandler client : ChattyServer.activeClientList)
				client.os.writeUTF(this.name + ": " + line);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
