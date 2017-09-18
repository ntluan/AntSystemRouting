import org.graphstream.graph.*;
import org.graphstream.algorithm.generator.*;
import org.graphstream.algorithm.*;
import org.graphstream.graph.implementations.*;
import java.util.*;
import org.graphstream.stream.*;
import org.graphstream.algorithm.Dijkstra;




public class AntSystem {
	private static final double maxcost=1000000;
	private static final double zo=0.7;
	public static class Ant{
		private int antID;
		private String antName;
		private Node currNode;
		private List<String> path;
		private List<Edge> edgePath;
		private Node startVertex;
		private Node endIDVertex;
		private double cost;
		private List<String> bestpath;
		private double bestcost;

		private final double alpha=10.0;
		private final double beta=3.0;

		private String deltaname;
// Init
		Ant(Graph graph, int antID, String antName, Node startVertex, Node endIDVertex ){
			this.antID = antID;
			this.antName = antName;
			this.startVertex = startVertex;
			this.endIDVertex = endIDVertex;
			this.currNode = startVertex;
			this.path = new ArrayList<String>();
			this.bestpath = new ArrayList<String>();
			this.bestcost = maxcost;
			this.cost = 0;
			this.deltaname = "delta"+Integer.toString(antID);
			this.edgePath = new ArrayList<Edge>();
			currNode.setAttribute("visited");
			path.add(currNode.toString());
		}
// Them delta cua kien cho graph
		private void addDeltaToGraph(Graph graph){
			for(String name: bestpath){
				Edge edge = graph.getEdge(name);
				double delta = edge.getAttribute(deltaname);
				edge.setAttribute(deltaname, delta+1/getbestcost());
			}
		}

		public double getbestcost(){
			return bestcost;
		}

		public double getcost(){
			return cost;
		}

		public int getAntID(){
			return antID;
		}
		
		public String getName(){
			return antName;
		}

		public Node getcurrentNode(){
			return currNode;
		}

		public List<String> getpath(){
			return path;
		}

		public List<String> getbestpath(){
			return bestpath;
		}

		public void clear(Graph graph){
			this.currNode = startVertex;
			for(Node node: graph){
				node.removeAttribute("visited");
			}
			for(Edge edge: graph.getEachEdge()){
				edge.removeAttribute("visited");
			}
			this.path = new ArrayList<String>();
			path.add(currNode.toString());
			cost = 0;
		}

		public double getCost(){
			double sum = 0.0;
			for(Edge edge: edgePath){
				double weight = edge.getAttribute("weight");
				sum+=weight;
			}
			cost = sum;
			return cost;
		}

		public void addToPath(){

		}
		public String toString(){
			return "nope";
		}

		private void moveTo(Node target){//Di den node, ham phu cho walk
			if(!target.hasAttribute("visited")){
				this.currNode = target;
				target.setAttribute("visited");
				path.add(target.toString());
			}
		}

		private void walk(Node node, Edge edge){// Thuc hien di sau khi da quyet dinh
				moveTo(edge.getOpposite(node));
				double weight = edge.getAttribute("weight");
			//Cap nhat moi cho Kien
				edgePath.add(edge);//Them canh vao path
				cost= getCost();// Them chi phi
				edge.setAttribute(deltaname, 1/cost);//Cap nhat delta cua kien
		}

		private boolean halt(Graph graph, Edge edge, Node node){//Quyet dinh xem co di ko theo xac suat dc tinh
			Random random = new Random();
			double  n = random.nextDouble();
			if(n < caculate_propability(edge, node, graph)){
				return true;
			}
			return false;
		}

		private double caculate_propability(Edge edge, Node node, Graph graph){//Tinh xac suat theo cong thuc o paper
			double thetaAbove = edge.getAttribute("theta");
			double num1Above = Math.pow(thetaAbove, alpha);
			double nuyAbove = edge.getAttribute("nuy");
			double num2Above = Math.pow(nuyAbove, beta);
			double numberAbove = num1Above*num2Above;
			double sum = 0.0;
			for(Edge e: node.getEachEdge()){
				if(edge.hasAttribute("visited")){
					double theta = e.getAttribute("theta");
					double num1 = Math.pow(theta, alpha);
					double nuy = e.getAttribute("nuy");
					double num2 = Math.pow(nuy, beta);
					double number = num1*num2;
					sum+=number;
				}
				//System.out.println("Nuiberrrrrrrrrrrrrrr::::::"+number);

			}
			if(sum==0){

				return 0.1;
			}else{
				//System.out.println("Above::::::"+numberAbove);
				//System.out.println("Sum::::::"+sum);

				//System.out.println("Prob::::::"+numberAbove/sum);
				return numberAbove/sum;
			}
		}

		public void test(Graph graph){
			Node currnode = graph.getNode(0);
			double n=0;
			for(Edge edge: currNode.getEachEdge()){
					n+=caculate_propability(edge, currNode, graph);
			}
			System.out.println(n);
		}
		public boolean move(Graph graph){
			Node previous_node = currNode; int count=0;
			Random rand = new Random();
				while(currNode==previous_node){
					double n=0.0;
					double random = rand.nextDouble();
					for(Edge edge: currNode.getEachEdge()){
						if(!edge.getOpposite(currNode).hasAttribute("visited")){						
							if(random>=n && random<=n+caculate_propability(edge, currNode, graph)){
								walk(currNode, edge);
				//				System.out.println("Prob:::"+caculate_propability(edge, currNode, graph)+"n::"+n);
								return true;
							}
						}
						n=n+caculate_propability(edge, currNode, graph);

						++count;
						if(count > 1000){//Lap qua nhieu ma khong duoc thi di duong khac, do thi to thi cho to ra
							//Vidu count>1000
							clear(graph);//Cac thong so tro lai luc dau
							break;
						}
					}
						//System.out.println("=============="+n);

			}
				if(count==0) return false;  
			
			return true;
		}
//lap lien tuc cho den khi den diem cuoi (endIDVertex)
		public void iterate(Graph graph){
			int count=0;
			while(currNode!=endIDVertex){
				move(graph);//Kien di chuyen trong graph
				//System.out.println(currNode.toString()+"--------------");
				count++;
			}
			addDeltaToGraph(graph);//Them delta
			addPhormone(graph, 1);
			if(cost<bestcost){
				bestcost = cost;
				bestpath = path;
			}
		} 
	}
//cap nhat theta cho ca graph voi tat ca kien
	public static void addPhormone(Graph graph, int numberOfAnt){
		for(Edge edge: graph.getEachEdge()){
			double theta = edge.getAttribute("theta");
			double sum = calculateDeltaSum(edge, numberOfAnt);
			edge.setAttribute("theta", (theta+sum)*(1-zo));
		}
	}
//tinh tong cac delta
	public static double calculateDeltaSum(Edge edge, int numberOfAnt){
		double sum=0;
		for(int i=0; i<numberOfAnt;i++){
			if(edge.hasAttribute("delta"+Integer.toString(i))){
				double num = edge.getAttribute("delta"+Integer.toString(i));
				sum+=num;
			}
		}
		return sum;
	}
//Khoi tao graph voi cac thong so weight, nuy, theta
	public static void makeGraph(Graph graph){
		// Add weight for edges 
		for(Edge edge: graph.getEachEdge()){
			Random rand = new Random();
			double x = rand.nextDouble()+10;
			edge.setAttribute("weight", x);
		}
		
		//Add nuy for edges
		for(Edge edge: graph.getEachEdge()){
			double weight = edge.getAttribute("weight");
			edge.setAttribute("nuy", 1.0/weight);
		}

		//Add theta for edges
		for(Edge edge: graph.getEachEdge()){
			edge.setAttribute("theta", .1);
		}	
	}

	public static void main(String args[]) {
		List<String> bestpath = new ArrayList<String>();
		double bestcost=10000;

		Graph graph = new SingleGraph("Random");
		    Generator gen = new RandomGenerator(50);
		    gen.addSink(graph);
		    gen.begin();
		    for(int i=0; i<100; i++)
		        gen.nextEvents();
		    gen.end();
		
		makeGraph(graph);
		//make dymanicity
		Random rand = new Random();


		Node a = graph.getNode(0);
		Node b = graph.getNode(1);
		

		for(int i=0; i<450; i++){
			Ant ant0 = new Ant(graph, 0, "ant0", a, b);
			ant0.iterate(graph);
			//ant0.test(graph);
			addPhormone(graph, 1);
			ant0.clear(graph);

			//dynamicity co the bo de test graph tinh
			/*double num = rand.nextDouble();
			for(Edge edge: graph.getEachEdge()){
				int change = rand.nextInt(200);
				if(change==0){
					double weight = edge.getAttribute("weight");
					edge.setAttribute("weight", weight+num);
				}else if(change==1){
					double weight = edge.getAttribute("weight");
					if(weight-num>=0){
						edge.setAttribute("weight", weight-num);
					}
				}
				double weight = edge.getAttribute("weight");
				edge.setAttribute("nuy",1.0/weight);
				//edge.setAttribute("delta0", 0.0);
			}*/
			//dynamicity

			Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");
			dijkstra.init(graph);
			dijkstra.setSource(a);
			dijkstra.compute();
			if(bestcost>ant0.getbestcost()){
				bestpath = ant0.getbestpath();
				bestcost = ant0.getbestcost();
			}
			System.out.println("dijkstra:::::"+dijkstra.getPath(b));
			System.out.printf("dijkstra::::::: %s->%s:%10.2f%n", dijkstra.getSource(), b,
								dijkstra.getPathLength(b));

			System.out.println(ant0.getbestpath().toString()+":::::::::::"+
			String.valueOf(ant0.getbestcost()));
			ant0.clear(graph);
			System.out.println("================================================");
			System.out.println("================edge");			
		}
		System.out.println("bestpath:::"+bestpath);
		System.out.println("bestcost:::"+bestcost);
	}
}
//



