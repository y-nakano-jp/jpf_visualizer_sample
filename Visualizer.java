import processing.core.*; 
import processing.xml.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*;

import java.lang.Thread;
import java.lang.InterruptedException;
import java.io.UnsupportedEncodingException;
import java.util.Queue;
import net.arnx.jsonic.JSON;

class EventServer implements Runnable{
	private Queue<String> event_queue = null;
	private ServerSocket server = null;
	private BufferedReader reader = null;
	private Socket socket = null;
	public void setSharedQueue(Queue<String> queue){
		event_queue = queue;
	}
	public void run(){
		if(event_queue == null)return;
	
		try{	
			server = new ServerSocket(12345);
		}catch(IOException e){
			System.err.println("open failed");
			return;
		}
		try{
			socket = server.accept();
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
		}catch(Exception e){
			return;
		}
		for(;;){
			try{
				String text = "";
				int c = reader.read();
				if(c == -1){
					break;
				}
				while(c != '\0'){
					text += (char)c;
					c = reader.read();
				}
				if(text.equals("end")){
					break;
				}
				synchronized(event_queue){
					event_queue.offer(text);
				}
			}catch(IOException e){
			}
		}
		try{
			reader.close();	
			socket.close();
			server.close();
		}catch(Exception e){
		}
	}	
}
 
public class Visualizer extends PApplet{
	static class Layer{
		public Number state_number = -1;
		public Number thread_number = -1;
		private List<Number> states = null;
		public Layer(){
			states = new LinkedList<Number>();
		}
		public void set_state_number(Number number){
			state_number = number;
		}
		
		public void set_thread_number(Number number){
			thread_number = number;
		}
		public void add_states(Number s){
			if(states != null){
				states.add(s);
			}
		}
	}
	private int current_layer = 0;
	static private List<Layer> layers = null;
	static private Queue<String> event_queue = null;
	private PFont font = null;
	private int height = 0;
	static public void create_layer(){
		layers = new LinkedList<Layer>();
		if(layers == null)return;
		Layer l = new Layer();
		l.set_state_number(-1);
		l.set_thread_number(-1);
		layers.add(l);
	}
	private void push_layer(Number state,Number thread){
		if(layers == null)return;
		Layer l = new Layer();
		l.set_state_number(state);
		l.set_thread_number(thread);
		layers.get(current_layer).add_states(state);
		layers.add(l);
		current_layer += 1;
	}
	private void pop_layer(Number state){
		if(layers == null)return;
		layers.remove(current_layer);
		current_layer -= 1;
	}

	public void setup() {
		size(800, 600,P3D);
		fill(0,0,0);
		font = loadFont("TlwgTypo-72.vlw");
		textFont(font,18);
		frameRate(60);
		camera(	300,300,200,	//eye_x, eye_y, eye_z
			0,0,0,		//lookat x y z
			0,0,-1);		//up x y z
		
	}
	private boolean decode_json(String str){
		HashMap hash = null;
		try{ 
			hash = (HashMap)JSON.decode(str);
		}catch(Exception e){
			return false;
		}
		if(((String)hash.get("e")).equals("a")){
			height += 5;
			this.push_layer(
				((Number)hash.get("id")),
				((Number)hash.get("tn")));
		}else{
			height -= 5;
			this.pop_layer(((Number)hash.get("id")));
		}

		camera(	1000,1000,100 + height,	//eye_x, eye_y, eye_z
			0,0,height - 100,		//lookat x y z
			0,0,-1);		//up x y z
		return true;
	}
	private void draw_layers(){
		int offset = 0;
		for(int i = 0; i < layers.size(); i++){
			Layer layer = layers.get(i);
			randomSeed(layer.thread_number.longValue());

			if(i > 1 && layer.thread_number.floatValue() != layers.get(i - 1).thread_number.floatValue()){
				rotateZ((float)(1.2 * layer.thread_number.floatValue()));
			}
			fill(random(0,255),random(0,255),random(0,255),128);
			translate(20, 0, 5);
			noStroke();
			box(40,40,10);
		}	
	}

	private void job(int i){
		String str = null;
		synchronized(event_queue){
			str = event_queue.poll();
		}
		if(str != null){
			decode_json(str);
		}
	}
	public void draw() {

		background(0,0,0);
		
		stroke(255,0,0);
		line(0,0,0,1000,0,0);

		stroke(0,255,0);
		line(0,0,0,0,1000,0);

		stroke(0,0,255);
		line(0,0,0,0,0,1000);

		draw_layers();

		if(event_queue != null){
			for(int i = 0; i < 10; i++){
				job(i);
			}
		}
	}
	public static  void main(String[] args){

		create_layer();
		event_queue = new LinkedList<String>();
		EventServer event_server = new EventServer();
		event_server.setSharedQueue(event_queue);

		Thread thread = new Thread(event_server);
		thread.start();

		PApplet.main(new String[] { "--bgcolor=#DFDFDF", "Visualizer" });
  	}
}
