package com.demon.game.rl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RLClientOnlyHole6x5 {
	
	static int STATE;
		
	static int LAYER;
	static int STATE_NUM;  // 8 x 5  的俄罗斯世界  8 ^ 5 = 32768
	static int ACTION_NUM;   // one block 的 20种可能的位置
		
	static int HEIGHT;
	static int WIDTH;
		
		
	static final int BLOCK_NUM = 7;
		
	static String QTableFile = "";
	static String QCompressFile = "";
	
	RLClientOnlyHole6x5(int layer, int height, int width, String fileName, String targetName){
		this.LAYER = layer;
		this.HEIGHT = height;
		this.WIDTH  = width;
		this.QTableFile = fileName;
		this.QCompressFile = targetName;
		
		this.STATE = (int) Math.pow(layer + 1, width);
		this.STATE_NUM = STATE + 1000;
		this.ACTION_NUM = width * 4;
		
	}
	
	static int[][][] BLOCK_SHAPE = {
			{ { 0, 0, 0, -1, 0, 1, 1, -1 },{ 0, 0, 1, 0, -1, 0,  1, 1 },{ 0, 0, 0, -1, 0, 1, -1,  1 },{ 0, 0, 1, 0, -1, 0, -1, -1 } },
			{ { 0, 0, 0, -1, 0, 1, 1,  1 },{ 0, 0, 1, 0, -1, 0, -1, 1 },{ 0, 0, 0, -1, 0, 1, -1, -1 },{ 0, 0, 1, 0, -1, 0,  1, -1 } },
			{ { 0, 0, 0,  1, 1, 0, 1, -1 },{ 0, 0, -1, 0, 0, 1, 1, 1},{ 0, 0, 0, -1, -1, 0, -1, 1 },{ 0, 0, 1, 0, 0, -1, -1, -1 } },
			{ { 0, 0, 0, -1, 1, 0, 1, 1},{ 0, 0, 1, 0, 0, 1, -1, 1 },{ 0, 0, 0, 1, -1, 0, -1, -1 },{ 0, 0, -1, 0, 0, -1, 1, -1} },
			{ { 0, 0, 0, -1, 0, 1, -1, 0},{ 0, 0, -1, 0, 1, 0, 0, -1 },{0, 0, 0, -1, 0, 1, 1, 0},{ 0, 0, -1, 0, 1, 0, 0, 1 } },
			{ { 0, 0, -1, 0, -2, 0, 1, 0},{ 0, 0, 0, -1, 0, -2, 0, 1 },{0, 0, -1, 0, 1, 0, 2, 0 },{ 0, 0, 0, -1, 0, 1, 0, 2 } },
			{ { 0, 0, -1, 0, 0, -1, -1, -1 },{ 0, 0, 0, -1, 1, 0, 1, -1 },{ 0, 0, 0, 1, 1, 0, 1, 1 },{ 0, 0, -1, 0, 0, 1, -1, 1 } }
	};// 7种形状(长L| 短L| 反z| 正z| T| 直一| 田格)，4种朝向(上左下右)，8:每相邻的两个分别为x，y
	
	static int[][][] BLOCK_DOWN = {
			{{0, 0, 0, 1, 1, -1},{1, 0, 1, 1},{0, 0, 0, -1, 0, 1},{1, 0, -1, -1}},	
			{{0, 0, 0, -1, 1, 1},{1, 0, -1, 1},{0, 0, 0, -1, 0, 1},{1, 0, 1, -1}},
			{{1, -1, 1, 0, 0, 1},{0, 0, 1, 1},{0, -1, 0, 0, -1, 1},{0, -1, 1, 0}},
			{{0, -1, 1, 0, 1, 1},{1, 0, 0, 1},{0, 0, -1, -1, 0, 1},{1, -1, 0, 0}},
			{{0, 0, 0, -1, 0, 1},{0, -1, 1, 0},{0, -1, 1, 0, 0, 1},{1, 0, 0, 1}},
			{{1, 0},{0, -2, 0, -1, 0, 0, 0, 1},{2, 0},{0, -1, 0, 0, 0, 1, 0, 2}},
			{{0, 0, 0, -1},{1, -1, 1, 0},{1, 0, 1, 1},{0, 0, 0, 1}}
	};
	
	static double[][][] Q;
	
	static class Map{
		
		class P{
			int s_;
			int eliminate;
			
			double reward;
			boolean done;
			
			P(){
			}
		}
		
		char[][] map;
		
		Map(){
			map = new char[HEIGHT][WIDTH];
			for (int i = 0; i < HEIGHT; ++i) {
				for (int j = 0; j < WIDTH; ++j) {
					map[i][j] = '.';
				}
			}
		}
		
		void initWithHeight(int[] height) {
			for (int i = 0; i < WIDTH; ++i) {
				for (int j = height[i]; j < HEIGHT; ++j) {
					map[j][i] = '#';
				}
			}
		}
		
		void randomInit() {
			for (int i = 0; i < WIDTH; ++i) {
				int j = new Random().nextInt(HEIGHT);
				for (; j < HEIGHT; ++j) {
					map[j][i] = '#';
				}
			}
		}
		
		Map(Map other){
			map = new char[HEIGHT][WIDTH];
			for (int i = 0; i < HEIGHT; ++i) {
				for (int j = 0; j < WIDTH; ++j) {
					map[i][j] = other.map[i][j];
				}
			}
		}
		
		void init(double random) {
			if (Math.random() > random) {
				for (int i = 0; i < HEIGHT; ++i) {
					for (int j = 0; j < WIDTH; ++j) {
						map[i][j] = '.';
					}
				}
			}
			else {
				for (int i = 0; i < HEIGHT; ++i) {
					for (int j =0; j < WIDTH; ++j) {
						map[i][j] = Math.random() > 0.2 ? '.' : '#';
					}
				}
			}
			//eliminate();
		}
		
		void build(int b, int o, int x, int y) {
			int[] dir = BLOCK_SHAPE[b][o];
			for (int i = 0; i < 4; ++i) {
				int nx = x + dir[2 * i];
				int ny = y + dir[2 * i + 1];
				map[nx][ny] = '#';
			}
		}
		
		char[] clone(char[] data) {
			char[] aux = new char[data.length];
			for (int i = 0; i < data.length; ++i) {
				aux[i] = data[i];
			}
			return aux;
		}
		
		int eliminate() {
			
			List<Integer> eliminates = new ArrayList<>();
			List<char[]> aux = new ArrayList<>();
			
			for (int i = 0; i < HEIGHT; ++i) {
				boolean canEle = true;
				for (int j = 0; j < WIDTH; ++j) {
					if (map[i][j] == '.') {
						canEle = false;
						break;
					}
				}
				if (canEle) eliminates.add(i);
				else {
					aux.add(clone(map[i]));
				}
			}
			
			int ele = eliminates.size();
			return ele;
			
//			if (ele == 0) return 0;
//			
//			for (int i = 0; i < ele; ++i) {
//				for (int j = 0; j < WIDTH; ++j) {
//					map[i][j] = '.';
//				}
//			}
//			
//			int j = ele;
//			for (int i = 0; i < aux.size(); ++i) {
//				char[] tmp = aux.get(i);
//				for (int l = 0; l < WIDTH; ++l) {
//					map[j][l] = tmp[l];
//				}
//				j++;
//			}
//			
//			return ele;
		}
		
		int calHole() {
			// 计算空洞数
			int cnt = 0;
			for (int i = 0; i < WIDTH; ++i) {
				int j = 0;
				while (j < HEIGHT && map[j][i] == '.') j++;
				for (; j < HEIGHT; ++j) {
					if (map[j][i] == '.') {
						cnt ++;
					}
				}
			}
			return cnt;
		}
		
		static final int INF = 0x3f3f3f3f;
		int observation() {
			int[] h = new int[WIDTH];
			int min = INF;
			int max = -INF;
			for (int i = 0; i < WIDTH; ++i) {
				int j = 0;
				while (j < HEIGHT && map[j][i] == '.') ++j;
				h[i] = HEIGHT - j;
				max = Math.max(max, h[i]);
			}
			
			for (int i = 0; i < WIDTH; ++i) {
				h[i] = Math.max(max - LAYER, h[i]);
				min = Math.min(min, h[i]);
			}
			
			for (int i = 0; i < WIDTH; ++i) {
				h[i] -= min;
			}
			
			int state = 0;
			int[] pow = new int[WIDTH];
			Arrays.fill(pow, 1);
			for (int i = WIDTH - 2; i >= 0; --i) {
				pow[i] = pow[i + 1] * (LAYER + 1);
			}
			
			for (int i = 0; i < WIDTH; ++i) {
				state += h[i] * pow[i];
			}
			return state;
		}
		
		P step(int action, int b) {
			if (action == -1) {
				P ans = new P();
				ans.done = false;
				ans.reward = -1;
				ans.eliminate = 0;
				ans.s_ = observation();  // 非法下的状态
				return ans;
			}
			
			int x = action / 4;
			int o = action % 4;
			
			boolean done = false;
			
			// 计算y所在的位置
			int y = HEIGHT - 1;
			for (; y >= 0; --y) {
				if (valid(y, x, b, o) && (canArrive(y, x, b, o) || canMove(y, x, b, o))) {
					done = true;
					break;
				}
			}
			
			if (done) {  // 状态合法
				// 加入块前
				int h_1 = calHole();
				int e_1 = eliminate();
				// 加入块
				build(b, o, y, x);
				// 消除块
				int h_2 = calHole();
				int e_2 = eliminate();
				
				int h = h_2 - h_1;
				int e = e_2 - e_1;
				
				P ans = new P();
				ans.done = true;
//				ans.reward = eliminate[e]; // 消除行则奖励？ 不给额外的奖励
				ans.reward = 0;
//				ans.reward += 1.0 * scorce / stage; // 平局分数
				ans.reward -= h;
				ans.eliminate = e_2;
				ans.s_ = observation();
				return ans;
			}
			else { // 没有合法状态
				P ans = new P();
				ans.done = false;
				ans.reward = -1;
				ans.eliminate = 0;
				ans.s_ = observation();  // 非法下的状态
				return ans;
			}
		}
		
		boolean valid(int i, int j, int b, int o) {
			int[] dir = BLOCK_SHAPE[b][o];
			for (int x = 0; x < 4; ++x) {
				int ni = i + dir[2 * x];
				int nj = j + dir[2 * x + 1];
				
				if (ni < 0 || ni >= HEIGHT || nj < 0 || nj >= WIDTH) return false; // 越界
				if (map[ni][nj] == '#') return false;  // 重合
			}
			return true;
		}
		
		// 判断当前位置的上方是否有方块
		boolean canArrive(int x, int y, int b, int o) {
			int[] dir  = BLOCK_SHAPE[b][o];
			
			for (int i = 0; i < 4; ++i) {
				int nx = x + dir[2 * i];
				int ny = y + dir[2 * i + 1];
				for (int j = nx - 1; j >= 0; --j) {
					if (map[j][ny] == '#') return false;
				}
			}
			return true;
		}
		
		
		// 左移1位 或者 右移1位的上方有障碍不
		boolean canMove(int x, int y, int b, int o) {
			return move(x, y, b, o, 1) || move(x, y, b, o, -1);
		}
		
		boolean move(int x, int y, int b, int o, int leftOrRight) {
			int[] dir  = BLOCK_SHAPE[b][o];
			
			// conflict
			for (int i = 0; i < 4; ++i) {
				int nx = dir[2 * i] + x;
				int ny = dir[2 * i + 1] + y;
				ny += leftOrRight;
				if (nx < 0 || nx >= HEIGHT || ny < 0 || ny >= WIDTH || map[nx][ny] == '#') return false;
				for (int j = nx - 1; j >= 0; --j) {
					if (map[j][ny] == '#') return false;
				}
			}
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < HEIGHT; ++i) {
				for (int j = 0; j < WIDTH; ++j) {
					sb.append(map[i][j] + (j + 1 == WIDTH ? "\n" : " "));
				}
			}
			return sb.toString();
		}
	}
	
	static class MapGenerater {
		
		HashMap<Integer, Integer> cnt;
		
		boolean valid() {
			int max = Collections.max(cnt.values());
			int min = Collections.min(cnt.values());
			return max - min <= 2;
		}
		
		int nextBlock() {
			int block = new Random().nextInt(BLOCK_NUM);
			cnt.put(block, cnt.getOrDefault(block, 0) + 1);
			if (valid()) return block;
			else {
				cnt.put(block, cnt.getOrDefault(block, 0) - 1);
				List<Integer> candicates = block_candicates();
				block = candicates.get(new Random().nextInt(candicates.size()));
				cnt.put(block, cnt.getOrDefault(block, 0) + 1);
				return block;
			}
		}
		
		List<Integer> block_candicates(){
			int max = Collections.max(cnt.values());
			List<Integer> candicates = new ArrayList<>();
			
			for (int key : cnt.keySet()) {
				int val = cnt.get(key);
				if (val < max) {
					candicates.add(key);
				}
			}
			return candicates;
		}
		
		MapGenerater(){
			cnt = new HashMap<>();
			// 初始化
			for (int i = 0; i < BLOCK_NUM; ++i) {
				cnt.put(i, 0);
			}
		}
	}
	
	static class RL{
		
		static double learning_rate = 0.01;
		static double reward_decay  = 0;
		static double e_greedy      = 0.9;
		
		static int chooseAction(int s, int b) {
			if (!check_state_exist(s)) return -1;  // 非法态
			if (Math.random() < e_greedy) {
				// choose best action
				double[] actions = Q[s][b];
				double max = -INF;
				
				List<Integer> candicates = new ArrayList<>();
				for (int i = 0; i < ACTION_NUM; ++i) {
					double val = actions[i];
					if (val >= max) {
						if (val == max) {
							candicates.add(i);
						}
						else {
							candicates.clear();
							candicates.add(i);
						}
						max = val;
					}
				}
				return candicates.get(new Random().nextInt(candicates.size()));
			}
			else {
				return new Random().nextInt(ACTION_NUM);
			}
		}
		
		static void learn(int s, int action, double reward, int s_, int b, int b_, boolean valid) {
			if (!check_state_exist(s)) return;
			double q_predict = Q[s][b][action]; // s 和 b 下的 当前action
			double q_target = 0;
			
			if (valid) {
				
				double q_table_max = -INF;
				
				for (int i = 0; i < ACTION_NUM; ++i) {
					double val = Q[s_][b_][i];
					if (q_table_max < val) {
						q_table_max = val;
					}
				}
				
				q_target = reward + reward_decay * q_table_max;
			}
			else {
				q_target = reward;
			}
			
			Q[s][b][action] += learning_rate * (q_target - q_predict);
		}
		
		static boolean check_state_exist(int s) {
			return s < STATE;
		}
	}
	
	static class Node implements Comparable<Node>{
		double rate;
		int scorce;
		int stage;
		
		Map env;
		
		Node(double rate, int scorce, int stage, Map env){
			this.rate = rate;
			this.scorce = scorce;
			this.stage = stage;
			this.env = env;
		}

		@Override
		public int compareTo(Node o) {
			return Double.compare(this.rate, o.rate);
		}
	}
	
	static int[] eliminate = {0, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39};
	static final int INF = 0x3f3f3f3f;
	static void update(int episode) { // 训练 其中 一个 block 的最佳位置
		
		int max_scorce = 0;
//		int peek_scorce = 400;
//		double average_max_scorce = 0;
		
		int times = 0;
		int threshold_times = 100000000;
		
//		Queue<Node> queue = new PriorityQueue<>();
//		queue.offer(new Node(INF, 0, 1, new Map()));
//		int topK = 10000;
		
		while (true) {
			// 重新开一轮
			Map env;
			int scorce;
			int cur_scorce;
			
				env = new Map();
				if (Math.random() > 0.6) env.randomInit();
				
//				for (int i = 0; i < 5; ++i) {
//					env.map[9][i] = '#';
//				}
				
//				env.map[8][2] = '#';
//				env.map[8][3] = '#';
//				env.map[7][2] = '#';
//				env.map[7][3] = '#';
//				env.map[9][3] = '.';
				
				scorce = 0;
				cur_scorce = 0;
//			else {
//				int size = queue.size();
//				Node c = queue.toArray(new Node[0])[new Random().nextInt(size)];
//				// 克隆当前环境
//				env        = new Map(c.env);
//				scorce     = c.scorce;
//				stage      = c.stage;
//				cur_scorce = c.scorce;
//			}
			
			// initial observation
			int s = env.observation();
			
			MapGenerater generator = new MapGenerater();
			int b = generator.nextBlock();
			
			while (true) {
				try {
//					Thread.sleep(100);   //渲染时间
					
					int action = RL.chooseAction(s, b);  // 根据当前状态，和给定的b选择动作
					
					Map.P p = env.step(action, b); // 改变环境
					
					// RL learn from this transition
					int b_ = generator.nextBlock();
					RL.learn(s, action, p.reward, p.s_, b, b_, p.done);
					
					// 改变状态
					s = p.s_;
					b = b_;
					
					if (!p.done) { // 状态非法, 直接跳出
//						System.err.println("状态非法");
						scorce = 0;
						break;
					}
					
					scorce = eliminate[p.eliminate];
					
					if (scorce > max_scorce) {
						System.out.println(env);
						System.out.println("当前回合消除的最大分数：" + scorce);
						max_scorce = scorce;
					}
					
					
					times ++;
					
					if (times > threshold_times) {
						System.out.println(env);
						System.out.println("当前累积行数： " + p.eliminate);
						writeQLearningTable();
						CompressQTable.compress(QTableFile, QCompressFile, STATE);
						System.out.println("持久化完毕..." + new Date());
						times = 0;
					}
						
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	static void initQLearningTable() {
		if (FileHelper.fileExist(QTableFile)) {
			System.out.println("加载数据...");
			Q = new double[STATE_NUM][BLOCK_NUM][ACTION_NUM];
			DReader reader = new DReader(QTableFile);
			int cnt = 0;
			while (reader.hasNext()) {
				String line = reader.next();
				String[] data = line.split(" ");
				for (int i = 0; i < 20; ++i) {
					Q[cnt / 7][cnt % 7][i] = Double.parseDouble(data[i]);
				}
				cnt ++;
			}
		}
		else {
			Q = new double[STATE_NUM][BLOCK_NUM][ACTION_NUM];
		}
	}
	
	static void writeQLearningTable() {
		if (FileHelper.fileExist(QTableFile)) {
			System.out.println("清理完毕");
			FileHelper.clearFile(QTableFile);
		}
		DWriter out = new DWriter(QTableFile);
		int n = Q.length;
		int m = Q[0].length;
		int l = Q[0][0].length;
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j) {
				StringBuilder sb = new StringBuilder();
				for (int k = 0; k < l; ++k) {
					sb.append(Q[i][j][k] + (k + 1 == l ? "" : " "));
				}
				out.println(sb.toString());
			}
		}
		out.close();
	}
	
	static void train() {
		// 构造 QLearning Table
		initQLearningTable();
		
		// 学习
		update(300);
	}
	
	static final boolean debug = false;
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		if (debug) {
			Map env = new Map();
			env.initWithHeight(new int[] {0, 3, 4, 9, 4});
//			for (int i = 0; i < env.HEIGHT; ++i) {
//				for (int j = 0; j < env.WIDTH; ++j) {
//					env.map[i][j] = Math.random() > 0.7 ? '.' : '#';
//				}
//			}
			
//			for (int i = 0; i < 5; ++i) {
//				env.map[9][i] = '#';
//			}
			
//			env.map[8][2] = '#';
//			env.map[8][3] = '#';
//			env.map[7][2] = '#';
//			env.map[7][3] = '#';
//			env.map[9][3] = '.';
			
			System.out.println(env.calHole());
			
			System.out.println(env);
			System.out.println(env.observation());
			System.out.println(Integer.toString(env.observation(), 11));
			return;
		}
		
		RLClientOnlyHole6x5 rl = new RLClientOnlyHole6x5(7, 7, 5, "QTable_7x5", "NoHole_7x5.txt");
		rl.train();
	}
}
