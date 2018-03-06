
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue; // Java �����Զ������˿�

public class Main {
	
	static final boolean D = false;
	static final int INF = 0x3f3f3f3f;
	
	static final int MAPWIDTH  = 10;
	static final int MAPHEIGHT = 20;
	
	static final int BLOCK_NUM = 7; // ������
	
	static int[][][] BLOCK_SHAPE = {
			{ { 0, 0, 0, -1, 0, 1, 1, -1 },{ 0, 0, 1, 0, -1, 0,  1, 1 },{ 0, 0, 0, -1, 0, 1, -1,  1 },{ 0, 0, 1, 0, -1, 0, -1, -1 } },
			{ { 0, 0, 0, -1, 0, 1, 1,  1 },{ 0, 0, 1, 0, -1, 0, -1, 1 },{ 0, 0, 0, -1, 0, 1, -1, -1 },{ 0, 0, 1, 0, -1, 0,  1, -1 } },
			{ { 0, 0, 0,  1, 1, 0, 1, -1 },{ 0, 0, -1, 0, 0, 1, 1, 1},{ 0, 0, 0, -1, -1, 0, -1, 1 },{ 0, 0, 1, 0, 0, -1, -1, -1 } },
			{ { 0, 0, 0, -1, 1, 0, 1, 1},{ 0, 0, 1, 0, 0, 1, -1, 1 },{ 0, 0, 0, 1, -1, 0, -1, -1 },{ 0, 0, -1, 0, 0, -1, 1, -1} },
			{ { 0, 0, 0, -1, 0, 1, -1, 0},{ 0, 0, -1, 0, 1, 0, 0, -1 },{0, 0, 0, -1, 0, 1, 1, 0},{ 0, 0, -1, 0, 1, 0, 0, 1 } },
			{ { 0, 0, -1, 0, -2, 0, 1, 0},{ 0, 0, 0, -1, 0, -2, 0, 1 },{0, 0, -1, 0, 1, 0, 2, 0 },{ 0, 0, 0, -1, 0, 1, 0, 2 } },
			{ { 0, 0, -1, 0, 0, -1, -1, -1 },{ 0, 0, 0, -1, 1, 0, 1, -1 },{ 0, 0, 0, 1, 1, 0, 1, 1 },{ 0, 0, -1, 0, 0, 1, -1, 1 } }
	};// 7����״(��L| ��L| ��z| ��z| T| ֱһ| ���)��4�ֳ���(��������)��8:ÿ���ڵ������ֱ�Ϊx��y
	
	static int[][][] BLOCK_DOWN = {
			{{0, 0, 0, 1, 1, -1},{1, 0, 1, 1},{0, 0, 0, -1, 0, 1},{1, 0, -1, -1}},	
			{{0, 0, 0, -1, 1, 1},{1, 0, -1, 1},{0, 0, 0, -1, 0, 1},{1, 0, 1, -1}},
			{{1, -1, 1, 0, 0, 1},{0, 0, 1, 1},{0, -1, 0, 0, -1, 1},{0, -1, 1, 0}},
			{{0, -1, 1, 0, 1, 1},{1, 0, 0, 1},{0, 0, -1, -1, 0, 1},{1, -1, 0, 0}},
			{{0, 0, 0, -1, 0, 1},{0, -1, 1, 0},{0, -1, 1, 0, 0, 1},{1, 0, 0, 1}},
			{{1, 0},{0, -2, 0, -1, 0, 0, 0, 1},{2, 0},{0, -1, 0, 0, 0, 1, 0, 2}},
			{{0, 0, 0, -1},{1, -1, 1, 0},{1, 0, 1, 1},{0, 0, 0, 1}}
	};
	
	static class PostureAndXY{
		int x;
		int y;
		int o;
		double scorce;
		
		PostureAndXY(int x, int y, int o, double scorce) {
			this.x = x;
			this.y = y;
			this.o = o;
			this.scorce = scorce;
		} 
	}
	
	static class MAP{
		
		int[][] map;	
		
		// ��ͼ��ʼ��
		MAP(){
			map = new int[MAPHEIGHT][MAPWIDTH];
		}
		
		MAP(int[][] map) {
			this.map = map;
		}
		
		// ��ͼ����, ���빹��Ϊ�Ϸ�̬
		void build(int b, int o, int x, int y) { //�����������ͣ���̬���������map
			int[] dir = BLOCK_SHAPE[b][o];
			int nx = MAPHEIGHT - y;
			int ny = x - 1;
			int n = dir.length;
			for (int i = 0; i < n; i += 2) {
				int dx = dir[i];
				int dy = dir[i + 1];
				map[dx + nx][dy + ny] = 1;
			}
		}
		
		// ��ͼ����
		int[][] load(){
			return map;
		}
		
		void clear() {
			map = new int[MAPHEIGHT][MAPWIDTH];
		}
	}
	
	static int[] transfer(int x, int y) {
		return new int[] {MAPHEIGHT - y, x - 1};
	}
	
	// �߶��ϵ�������꣬ ��С��
	static int maxBlockCor(int b, int o, int x, int y) {
		int[] npos = transfer(x, y);
		int[] dir  = BLOCK_SHAPE[b][o];
		int min = INF;
		for (int i = 0; i < 4; ++i) {
			int nx = npos[0] + dir[2 * i];
			min = Math.min(min, nx);
		}
		return min;
	}
		
	// �жϵ�ǰλ�õ��Ϸ��Ƿ��з���
	static boolean canArrive(int[][] map, int x, int y, int b, int o) {
		int[] npos = transfer(x, y);
		int[] dir  = BLOCK_SHAPE[b][o];
		
		for (int i = 0; i < 4; ++i) {
			int nx = npos[0] + dir[2 * i];
			int ny = npos[1] + dir[2 * i + 1];
			for (int j = nx - 1; j >= 0; --j) {
				if (map[j][ny] == 1) return false;
			}
		}
		return true;
	}
	
	
	// ����1λ ���� ����1λ���Ϸ����ϰ���
	static boolean canMove(int[][] map, int x, int y, int b, int o) {
		return move(map, x, y, b, o, 1) || move(map, x, y, b, o, -1);
	}
	
	static boolean move(int[][] map, int x, int y, int b, int o, int leftOrRight) {
		int[] npos = transfer(x, y);
		int[] dir  = BLOCK_SHAPE[b][o];
		
		// conflict
		for (int i = 0; i < 4; ++i) {
			int nx = dir[2 * i] + npos[0];
			int ny = dir[2 * i + 1] + npos[1];
			ny += leftOrRight;
			if (nx < 0 || nx >= 20 || ny < 0 || ny >= 10 || map[nx][ny] == 1) return false;
			for (int j = nx - 1; j >= 0; --j) {
				if (map[j][ny] == 1) return false;
			}
		}
		return true;
	}
	
	
	static boolean onGround(int[][] map, int x, int y, int b, int o) {
		int nx = MAPHEIGHT - y;
		int ny = x - 1;
		
		int max = 0;
		int[] dir = BLOCK_SHAPE[b][o];
		for (int i = 0; i < 8; i += 2) {
			int dx = dir[i];
			max = Math.max(nx + dx, max);
		}
		
		if (max + 1 == MAPHEIGHT) return true;
		
		boolean ground = false;
		int[] downBoundary = BLOCK_DOWN[b][o]; 
		
		for (int i = 0; i < downBoundary.length; i += 2) {
			int dx = downBoundary[i];
			int dy = downBoundary[i + 1];
			ground |= map[dx + nx + 1][dy + ny] == 1;
		}
		
		return ground;
	}
	
	//���Ϸ����ж�
	static boolean valid(int[][] map, int x, int y, int b, int o) {
		int nx = MAPHEIGHT - y;
		int ny = x - 1;
		
		int[] dir = BLOCK_SHAPE[b][o];
		for (int i = 0; i < 8; i += 2) {
			int dx = dir[i];
			int dy = dir[i + 1];
			
			int xi = nx + dx;
			int yi = ny + dy;
			
			if (xi < 0 || xi >= MAPHEIGHT || yi < 0 || yi >= MAPWIDTH) return false;
			if (map[xi][yi] == 1) return false;
		}
		
		return true;
	}
	
	static class Range{
		int i;
		int j;
		Range (int i, int j){
			this.i = i;
			this.j = j;
		}
	}
	
	static class MAPINFO{
		int nh;  //��ǰ���п�����߶�
		int nw;  //��ǰ���п�������
		int block_height; // ��ǰ��ĸ߶�
		
		int eliminate; // ��������������
		int hole;      // �ն���  (ʵ���ĳͷ����ȸ����鶴�����ֲ�)
		
		int virtual_hole;   // �鶴
		int valid_hole;     // ʵ��
		
		int fit_env;  // �뻷������ϳ̶�
		
		int satiation; // ������
		
		Set<Integer> range;
		
		int[][] map;
		
		MAPINFO(int[][] map) {
			this.map = map;
			range = new HashSet<>();
		}
		
		int platform; // ƽ̨��˵���˾��ǿ�ȴ��ڵ���2��ƽ̨
		int gaoditai_1;
		int gaoditai_2;
		
		void calPlatformInfo() {
			
			int[] height = new int[10];
			for (int i = 0; i < 10; ++i) {
				int j = 0;
				while (j < 20 && map[j][i] == 0) j++;
				height[i] = MAPHEIGHT - j;
			}
			
			int cnt = 0;
			int prev_height = -1; //ǰһ�� �߶�
			for (int i = 0, t = 1; i < 10; ++i) {
				if (height[i] == prev_height) {
					t++;
				}
				else {
					if (t >= 2) cnt ++;
					t = 1;
				}
				prev_height = height[i];
			}
			
			this.platform = cnt;
			
			// ����ߵͲ�Ϊ1�ĸߵ�̨
			cnt = 0;
			for (int i = 1; i < 10; ++i) {
				if (height[i] - height[i - 1] == 1) {
					cnt ++;
				}
				else if (height[i] - height[i - 1] == -1) {
					cnt ++;
				}
			}
			
			gaoditai_1 = cnt;
			
			// ����ߵͲ�Ϊ2�ĸߵ�̨
			cnt = 0;
			for (int i = 1; i < 10; ++i) {
				if (height[i] - height[i - 1] == 2) {
					cnt ++;
				}
				else if (height[i] - height[i - 1] == -2) {
					cnt ++;
				}
			}
			
			gaoditai_2 = cnt;
		}
		
		int calHole() {
			return calHoleInRange(0, MAPWIDTH - 1);
		}
		
		int calHoleInRange(int i, int j) {
			int kongdong = 0;
			for (int x = i; x <= j; ++x) {
				int y = 0;
				while (y < MAPHEIGHT && map[y][x] == 0) y++;
				for (; y < MAPHEIGHT; ++y) {
					if (map[y][x] == 0) {
						kongdong ++;
					}
				}
			}
			return kongdong;
		}
		
		
		int fitEnviroment(int b, int o, int x, int y) {
			int[] npos = transfer(x, y);
			int[] dir = BLOCK_SHAPE[b][o];
			int cnt = 0;
			for (int i = 0; i < 4; ++i) {
				int nx = npos[0] + dir[2 * i];
				int ny = npos[1] + dir[2 * i + 1];
				
				for (int dx = -1; dx <= 1; ++dx) {
					for (int dy = -1; dy <= 1; ++dy) {
						if (Math.abs(dx + dy) == 1) {
							int ni = nx + dx;
							int nj = ny + dy;
							if (nj < 0 || nj >= 10) cnt += 1;  //ǽ�ڵ���϶� ����
							if (ni < 0 || ni >= 20) cnt += 1;
							if (ni >= 0 && ni < 20 && nj >= 0 && nj < 10 && map[ni][nj] == 1) {
								cnt ++;
							}
						}
					}
				}
				
			}
			
			this.fit_env = cnt;
			return cnt;
		}
		
		// ���㵱ǰ�ܹ���������
		int calEliminate() {
			range.clear();
			for (int i = 0; i < MAPHEIGHT; ++i) {
				if (canEliminate(map[i])) {
					range.add(i);
				}
			}
			this.eliminate = range.size();
			return range.size();
		}
		
		void evaluate(){
			// ��ǰ���п�����߶�
			int max = 0;
			for (int i = 0; i < MAPWIDTH; ++i) {
				for (int j = 0, h = 0; j < MAPHEIGHT; ++j, ++h) {
					if (map[j][i] != 0) {
						max = Math.max(max, MAPHEIGHT - h);
						break;
					}
				}
			}
			nh = max;
			
			eliminate = calEliminate();
			
			// ����ն���
			int kongdong = 0;
			List<Pair> ps = new ArrayList<>();
			for (int i = 0; i < MAPWIDTH; ++i) {
				int j = 0;
				while (j < MAPHEIGHT && map[j][i] == 0) j++;
				for (; j < MAPHEIGHT; ++j) {
					if (map[j][i] == 0) {
						ps.add(new Pair(j , i));
						kongdong ++;
					}
				}
			}
			
			hole = kongdong;
			
			// ȷ��Ϊʵ�����ǿն�
			virtual_hole = virtualHoleCount(ps);
			valid_hole = hole - virtual_hole;
		}
		
		// ���㱥����
		int calSatiation() {
			int cnt = 0;
			for (int i = 0; i < 20; ++i) {
				int t = 0;
				for (int j = 0; j < 10; ++j) {
					if (map[i][j] == 1) ++t;
				}
				if (t >= 8) cnt ++;
				if (cnt >= 1 && t < 8) break;
				if (cnt >= 4) break;
			}
			this.satiation = cnt;
			return cnt;
		}
		
		int[][] dir = {{-1, 0},{1, 0},{0, -1},{0, 1}};
		
		// �ж��Ƿ�Ϊʵ�� bfs
		int virtualHoleCount(List<Pair> ps) {
			int cnt = 0;
			for (Pair p : ps) {
				if (isVirtualHole(p.i, p.j, new boolean[20][10])) {
					cnt ++;
				}
			}
			return cnt;
		}
		
		boolean isVirtualHole(int i, int j, boolean[][] v) {
			if (i <= 0 && j >= 0 || i <= 0 && j <= 9) return true;
			else {
				v[i][j] = true;
				for (int[] d : dir) {
					int nx = d[0] + i;
					int ny = d[1] + j;
					if (valid(nx, ny) && !v[nx][ny] && isVirtualHole(nx, ny, v) ) {
						return true;
					}
				}
				v[i][j] = false;
				return false;
			}
		}
		
		boolean valid(int x, int y) {
			return x >= 0 && x < MAPHEIGHT && y >= 0 && y < MAPWIDTH && map[x][y] == 0;
		}
		
		class Pair{
			int i;
			int j;
			Pair(int i, int j){
				this.i = i;
				this.j = j;
			}
		}
		
		boolean canEliminate(int[] row) {
			for (int i = 0; i < MAPWIDTH; ++i) {
				if (row[i] == 0) return false;
			}
			return true;
		}
		
		
		// ���㹵�ֵ����
		
		int deep;
		int calDeep(int b, int o, int x, int y) {
			int[] height = new int[MAPWIDTH];
			for (int i = 0; i < MAPWIDTH; ++i) {
				int j = 0;
				while (j < MAPHEIGHT && map[j][i] == 0) j++;
				height[i] = MAPHEIGHT - j;
			}
			
			
			int[] npos = transfer(x, y);
			int[] dir  = BLOCK_SHAPE[b][o];
			int min = 10;
			int max = -1;
			
			int lf = 0, rt = 0;
			for (int i = 0; i < 4; ++i) {
				int ny = npos[1] + dir[2 * i + 1];
				if (ny < min) {
					min = ny;
					if (ny - 1 == 0) lf = Math.abs(height[ny] - height[ny - 1]);
					else if (ny - 2 >= 0 && height[ny - 2] != height[ny - 1]) lf = Math.abs(height[ny] - height[ny - 1]);
					else lf = 0;
				}
				
				if (ny > max) {
					max = ny;
					if (ny + 1 == 9) rt = Math.abs(height[ny] - height[ny + 1]);
					else if (ny + 2 <= 9 && height[ny + 2] != height[ny + 1]) rt = Math.abs(height[ny] - height[ny + 1]);
					else rt = 0;
				}
			}
			
			this.deep = Math.max(lf, rt);
			return Math.max(lf, rt);
		}
		
		int calGouheInRange(int b, int o, int x) {
			int ny = x - 1; // ��Ӧ�ĺ�����
			
			int[] height = new int[MAPWIDTH];
			for (int i = 0; i < MAPWIDTH; ++i) {
				int j = 0;
				while (j < MAPHEIGHT && map[j][i] == 0) j++;
				height[i] = MAPHEIGHT - j;
			}
			
			int[] gouhes = new int[MAPWIDTH];
			gouhes[0] = Math.max(0, height[1] - height[0]);
			gouhes[9] = Math.max(0, height[8] - height[9]);
			
			// λ�� 1 - 8 �����
			for (int i = 1; i < 8; ++i) {
				int min = Math.min(height[i - 1] - height[i], height[i + 1] - height[i]);
				gouhes[i] = Math.max(0, min);
			}
			
			int[] dir = BLOCK_SHAPE[b][o];
			int max = 0;
			for (int i = 0; i < 4; ++i) {
				int yy = ny + dir[2 * i + 1];
				if (yy - 1 >= 0) max = Math.max(max, gouhes[yy - 1]);
				if (yy + 1 < MAPWIDTH) max = Math.max(max, gouhes[yy + 1]);
				max = Math.max(max, gouhes[yy]);
			}
			return max;
		}
		
		int calMaxGouHe() {
			
			int[] height = new int[MAPWIDTH];
			for (int i = 0; i < MAPWIDTH; ++i) {
				int j = 0;
				while (j < MAPHEIGHT && map[j][i] == 0) j++;
				height[i] = MAPHEIGHT - j;
			}
			
			int max = 0;
			
			// λ�� 0 �����
			max = Math.max(max, height[1] - height[0]);
			
			// λ��9 �����
			max = Math.max(max, height[8] - height[9]);
			
			// λ�� 1 - 8 �����
			for (int i = 1; i < 8; ++i) {
				int min = Math.min(height[i - 1] - height[i], height[i + 1] - height[i]);
				max = Math.max(max, min);
			}
			
			return max;
		}
		
		
		
		int calMaxHeight() {
			int max = -INF;
			int[] height = new int[MAPWIDTH];
			for (int i = 0; i < MAPWIDTH; ++i) {
				int j = 0;
				while (j < MAPHEIGHT && map[j][i] == 0) j++;
				height[i] = MAPHEIGHT - j;
				max = Math.max(max, height[i]);
			}
			return max;
		}
		
		int calMinHeight() {
			int min = INF;
			int[] height = new int[MAPWIDTH];
			for (int i = 0; i < MAPWIDTH; ++i) {
				int j = 0;
				while (j < MAPHEIGHT && map[j][i] == 0) j++;
				height[i] = MAPHEIGHT - j;
				min = Math.min(min, height[i]);
			}
			return min;
		}
		
		
		
		// ���� ��λ���µ�ʵ������
		int bottomHole;
		
		int calBottomHole(int b, int o, int x, int y) {
			int cnt = 0;
			int[] npos = transfer(x, y);
			int[] dir  = BLOCK_DOWN[b][o];
			
			for (int i = 0; i < dir.length; i += 2) {
				int nx = npos[0] + dir[i];
				int ny = npos[1] + dir[i + 1];
				
				for (int j = nx; j < 20; ++j) {
					if (map[j][ny] == 0) {
						if (!isVirtualHole(j, ny, new boolean[20][10])) cnt ++;
					}
				}
			}
			
			this.bottomHole = cnt;
			return cnt;
		}
		
		// ���㵱ǰ��ĸ߶�
		int calBlockHeight(int b, int o, int x, int y) {
			int max = 0;
			int[] npos = transfer(x, y);
			int[] dir  = BLOCK_SHAPE[b][o];
			for (int i = 0; i < 4; ++i) {
				int nx = npos[0] + dir[2 * i];
				int ny = npos[1] + dir[2 * i + 1];
				int j = nx;
				while (j < 20 && map[j][ny] == 0) j++;
				max = Math.max(max, MAPHEIGHT - j);
			}
			
			return block_height = max;
		}
	}
	
	static class BLOCKPOSINFO{
		int bh;  // ��ǰ��ĸ߶�
		int b;
		int o;
		int x;
		int y;
		
		BLOCKPOSINFO(int b, int o, int x, int y){
			this.b = b;
			this.o = o;
			this.x = x;
			this.y = y;
			evaluate();
		}
		
		void evaluate() {
			// ���㵱ǰ������߶�
			int max = 0;
			int[] npos = transfer(x, y);
			int[] dir  = BLOCK_SHAPE[b][o];
			for (int i = 0; i < 4; ++i) {
				int nx = npos[0] + dir[2 * i];
				int ny = npos[1] + dir[2 * i + 1];
				max = Math.max(max, MAPHEIGHT - nx);
			}
			this.bh = max;
		}
	}
	
	
	static final int ACTION_NUM = 5 * 4;   // one block �� 20�ֿ��ܵ�λ��
	
	static class MyMap{
		
		int layer;
		int height;
		int width;
		char[][] map;
		
		class P{
			int s_;
			int eliminate;
			
			double reward;
			boolean done;
			
			P(){
			}
		}
		
		MyMap(int layer, int height, int width, int[][] map20_10, int start){
			this.layer = layer;
			this.height = height;
			this.width = width;
			
			map = new char[height][width];
			int minRow = MAPHEIGHT - layer;
			
			for (int i = 0; i < width; ++i) {
				int j = 0;
				for (; j < MAPHEIGHT; ++j) {
					if (map20_10[j][i + start] == 1) break;
				}
				minRow = Math.min(minRow, j);
			}
			
			for (int j = 0; j < height; ++j) {
				for (int i = 0; i < width; ++i) {
					map[j][i] = map20_10[j + minRow][i +start] == 1 ? '#' : '.';
				}
			}
		}
		
		void init() {
			for (int i = 0; i < height; ++i) {
				for (int j = 0; j < width; ++j) {
					map[i][j] = '.';
				}
			}
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
		
		static final int INF = 0x3f3f3f3f;
		int observation() {
			int[] h = new int[width];
			int min = INF;
			int max = -INF;
			for (int i = 0; i < width; ++i) {
				int j = 0;
				while (j < height && map[j][i] == '.') ++j;
				h[i] = height - j;
				max = Math.max(max, h[i]);
			}
			
			for (int i = 0; i < width; ++i) {
				h[i] = Math.max(max - layer, h[i]);
				min = Math.min(min, h[i]);
			}
			
			for (int i = 0; i < width; ++i) {
				h[i] -= min;
			}
			
			int state = 0;
			int[] pow = new int[width];
			Arrays.fill(pow, 1);
			for (int i = width - 2; i >= 0; --i) {
				pow[i] = pow[i + 1] * (layer + 1);
			}
			
			for (int i = 0; i < width; ++i) {
				state += h[i] * pow[i];
			}
			return state;
		}
		
		
		boolean valid(int i, int j, int b, int o) {
			int[] dir = BLOCK_SHAPE[b][o];
			for (int x = 0; x < 4; ++x) {
				int ni = i + dir[2 * x];
				int nj = j + dir[2 * x + 1];
				
				if (ni < 0 || ni >= height || nj < 0 || nj >= width) return false; // Խ��
				if (map[ni][nj] == '#') return false;  // �غ�
			}
			return true;
		}
		
		// �жϵ�ǰλ�õ��Ϸ��Ƿ��з���
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
		
		// ����1λ ���� ����1λ���Ϸ����ϰ���
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
				if (nx < 0 || nx >= height || ny < 0 || ny >= width || map[nx][ny] == '#') return false;
				for (int j = nx - 1; j >= 0; --j) {
					if (map[j][ny] == '#') return false;
				}
			}
			return true;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < height; ++i) {
				for (int j = 0; j < width; ++j) {
					sb.append(map[i][j] + (j + 1 == width ? "\n" : " "));
				}
			}
			return sb.toString();
		}
	}
	
	static class DReader {
		
		private BufferedReader reader;
		private String line;
		
		public DReader(String fileName){
			try {
				File file = new File(fileName);
				FileReader fileReader = new FileReader(file);
				reader = new BufferedReader(fileReader);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		public boolean hasNext(){
			try {
				line = reader.readLine();
				if (line == null){
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return line != null;
		}
		
		public String next(){
			return line;
		}
	}
	
	static final int TYPE_7x5  = 0;
	static final int TYPE_10x5 = 1;
	
	static class RL{
		
		static int chooseActionFromTrainedData(int s, int b, int type) {
			if (type == TYPE_10x5) {
				return local_action_10x5[s][b];
			}
			else {
				return local_action_7x5[s][b];
			}
		}
	}
	
	static int cal_x(int[][] map, int b, int o, int y) {
		int x = 19;
		int nx = y + 1;
		int ny = MAPHEIGHT - x;
		for (; x >= 0; --x) {
			ny = MAPHEIGHT - x;
			if (valid(map, nx, ny, b, o) && (canArrive(map, nx, ny, b, o) || canMove(map, nx, ny, b, o))) {
				break;
			}
		}
		return ny;
	}
	
	static List<PostureAndXY> candicates;
	static boolean learnRL = false;

	static void test(MyMap env, int b, int o, int x) {
		// ����y���ڵ�λ��
		int y = 9;
		for (; y >= 0; --y) {
			if (env.valid(y, x, b, o) && (env.canArrive(y, x, b, o) || env.canMove(y, x, b, o))) {
				break;
			}
		}
		env.build(b, o, y, x);
		System.out.println(env);
	}
		
	// window_num = 0, 1, 2, 3 �ֱ��ʾ��0 - 4  2 - 6  3 - 7  5 - 9  
	static int[] window_index = {0, 2, 3, 5};
	static PostureAndXY candicate(int[][] map, int b, int type, int model) { // model �÷�ģʽ
		
		MyMap env;
		if (model == TYPE_10x5) {
			env = new MyMap(10, 10, 5, map, window_index[type]);
		}
		else {
			env = new MyMap(7, 7, 5, map, window_index[type]);
		}
		
		int s = env.observation();
		
		int action = RL.chooseActionFromTrainedData(s, b, model);
		
		int y = action / 4;
		int o = action % 4;
		
		y += window_index[type];
		
		int nx = y + 1;
		int ny = cal_x(map, b, o, y);
		
		if (valid(map, nx, ny, b, o) && onGround(map, nx, ny, b, o) && (canArrive(map, nx, ny, b, o) || canMove(map, nx, ny, b, o))) {
			return new PostureAndXY(nx, ny, o, 0);
		}
		return null;
	}
	
	static int window_num = 4;
	static List<PostureAndXY> compagin(int[][] map, int b, int model){
		List<PostureAndXY> ans = new ArrayList<>();
		for (int i = 0; i < window_num; ++i) {
			PostureAndXY tmp = candicate(map, b, i, model);
			if (tmp != null) ans.add(tmp);
		}
		return ans;
	}
	
	static PostureAndXY bestByIterator(int[][] map, int b) {
		PostureAndXY best = new PostureAndXY(-1, -1, -1, 0);
		double max = -INF;
		for (int x = 1; x <= 10; ++x) {
			for (int yy = 1; yy <= 20; ++yy) {
				for (int oo = 0; oo < 4; ++oo) {
					if (valid(map, x, yy, b, oo) && onGround(map, x, yy, b, oo) && (canArrive(map, x, yy, b, oo) || canMove(map, x, yy, b, oo))) {  // canArrive ������ ����һЩ�Ϸ�״̬
						if (inValidPos(map, b, oo, x, yy)) continue;
						double scorce = strategy(map, b, oo, x, yy);
						if (max < scorce) {
							best.o = oo;
							best.x = x;
							best.y = yy;
							best.scorce = scorce;
							max = scorce;
						}
					}
				}
			}
		}
		return best;
	}
	
	static boolean getScore(int[][] map, int b) {
		MAPINFO info = new MAPINFO(map);
		int gouhe = info.calMaxGouHe();
		int minHeight = info.calMinHeight();
		int maxHeight = info.calMaxHeight();
//		return gouhe > 3 || minHeight >= 8;
		return false;
	}
	
	static List<PostureAndXY> filter(List<PostureAndXY> choices, int[][] map, int b, int h){
		// ������ ʵ�� �� ���� <= 3
		List<PostureAndXY> candicates = new ArrayList<>();
		for (PostureAndXY c : choices) {
			if (noHole(map, b, c.o, c.x, c.y, 0) && noLargeGouHe(map, b, c.o, c.x, c.y, h) && noMoreHeight(map, b, c.o, c.x, c.y)) {
				candicates.add(new PostureAndXY(c.x, c.y, c.o, c.scorce));
				if (canLeftOrRight(map, b, c.o, c.x, c.y, 1) && noHole(map, b, c.o, c.x + 1, c.y, -1) 
						&& noLargeGouHe(map, b, c.o, c.x + 1, c.y, h) && noMoreHeight(map, b, c.o, c.x + 1, c.y)) {
					candicates.add(new PostureAndXY(c.x + 1, c.y, c.o, c.scorce));
				}
				if (canLeftOrRight(map, b, c.o, c.x, c.y, -1) && noHole(map, b, c.o, c.x - 1, c.y, -1) 
						&& noLargeGouHe(map, b, c.o, c.x - 1, c.y, h) && noMoreHeight(map, b, c.o, c.x - 1, c.y)) {
					candicates.add(new PostureAndXY(c.x - 1, c.y, c.o, c.scorce));
				}
			}
		}
		return candicates;
	}
	
	static int[] height_rate = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 4, 4, 4, 4, 2, 2, 1, 1, 1, 1, 1};
	static boolean noMoreHeight(int[][] map, int b, int o, int x, int y) {  // ���㵱ǰ���߶� ����������� �����
		MAP now = new MAP(clone(map));
		MAPINFO info = new MAPINFO(now.load());
		int h1 = info.calMaxHeight();
		now.build(b, o, x, y);
		
		info.calEliminate();
		eliminate(now.load());
		
		int h2 = info.calMaxHeight();
		
		return h1 <= 10 || h1 > 10 && h2 - h1 <= height_rate[h1];
	}
	
	static boolean canLeftOrRight(int[][] map, int b, int o, int x, int y, int diff) {
		x += diff;
		return valid(map, x, y, b, o) && onGround(map, x, y, b, o) && (canArrive(map, x, y, b, o) || canMove(map, x, y, b, o));
	}
	
	
	
	static boolean noLargeGouHe(int[][] map, int b, int o, int x, int y, int h) {
		MAP now = new MAP(clone(map));
		now.build(b, o, x, y);
		MAPINFO info = new MAPINFO(now.load());
		info.calEliminate();
		eliminate(now.load());
		int gouHe = info.calGouheInRange(b, o, x);
		return gouHe <= h;
	}
	
	static boolean noHole(int[][] map, int b, int o, int x, int y, int minus) { // û���κ�ʵ���ж�
		MAP now = new MAP(clone(map));
		MAPINFO info = new MAPINFO(now.load());
		int h_1 = info.calHole();
		
		now.build(b, o, x, y);
		info = new MAPINFO(now.load());
		int h_2 = info.calHole();
		
		// ���¿�ʮ�� �Ƿ���hole
		int hole = h_2 - h_1;
		
		return hole <= minus;
	}
	
	static PostureAndXY cal(int[][] map, int b, boolean isOppo) { // ��ǰ ���� block ���� ���λ�� �� ��̬
		if (isOppo) {
			PostureAndXY best = new PostureAndXY(-1, -1, -1, -1);
			double sum = 0.01;
			double max = -INF;
			double fit = 0;
			for (int x = 1; x <= 10; ++x) {
				for (int yy = 1; yy <= 20; ++yy) {
					for (int oo = 0; oo < 4; ++oo) {
						if (valid(map, x, yy, b, oo) && onGround(map, x, yy, b, oo) && (canArrive(map, x, yy, b, oo) || canMove(map, x, yy, b, oo))) {  // canArrive ������ ����һЩ�Ϸ�״̬
							Pair p = strategyToOppo(map, b, oo, x, yy);
							sum += p.fit;
							if (max < p.scorce) {
								max = p.scorce;
								fit = p.fit;
							}
						}
					}
				}
			}
			
			best.scorce = fit / sum;
			
			return best;
		}
		
		PostureAndXY best = bestByIterator(map, b);
		
		if (!getScore(map, b)) { // ����ģʽ
			
			List<PostureAndXY> choices = filter(compagin(map, b, TYPE_10x5), map, b, 3);
			
			if (choices.size() == 0) {
				learnRL = false;
				return best;
			}
			else {
				
				PostureAndXY[] greed = new PostureAndXY[5];
				boolean canGreedy = false;
				// ̰��һ��
				for (int x = 1; x <= 10; ++x) {
					for (int yy = 1; yy <= 20; ++yy) {
						for (int oo = 0; oo < 4; ++oo) {
							if (valid(map, x, yy, b, oo) && onGround(map, x, yy, b, oo) && (canArrive(map, x, yy, b, oo) || canMove(map, x, yy, b, oo))) { 
								if (inValidPos(map, b, oo, x, yy)) continue;
								if (greedy(map, b, oo, x, yy, greed)) {
									canGreedy = true;
								}
							}
						}
					}
				}
				
				// choose best
				if (canGreedy) {
					int j = 4;
					while (j >= 0 && greed[j] == null) j--;
					return greed[j];
				}
				
				double max = -INF;
				PostureAndXY next = new PostureAndXY(-1, -1, -1, 0);
				
				for (PostureAndXY candicate : choices) {
					double scorce = strategy(map, b, candicate.o, candicate.x, candicate.y);
					if (max < scorce) {
						next.o = candicate.o;
						next.x = candicate.x;
						next.y = candicate.y;
						next.scorce = scorce;
						max = scorce;
					}
				}
				
				// �����ǰmap �Ĺ������ ���ڵ���3 �����÷�ģʽ
				
				learnRL = true;
				return next;
			}
		}
		else {  // �÷�ģʽ
			List<PostureAndXY> choices =  filter(compagin(map, b, TYPE_7x5), map, b, 2);
			
			if (choices.size() == 0) {
				learnRL = false;
				return best;
			}
			else {
				
				PostureAndXY[] greed = new PostureAndXY[5];
				boolean canGreedy = false;
				// ̰��һ��
				for (int x = 1; x <= 10; ++x) {
					for (int yy = 1; yy <= 20; ++yy) {
						for (int oo = 0; oo < 4; ++oo) {
							if (valid(map, x, yy, b, oo) && onGround(map, x, yy, b, oo) && (canArrive(map, x, yy, b, oo) || canMove(map, x, yy, b, oo))) { 
								if (inValidPos(map, b, oo, x, yy)) continue;
								if (greedy(map, b, oo, x, yy, greed)) {
									canGreedy = true;
								}
							}
						}
					}
				}
				
				// choose best
				if (canGreedy) {
					int j = 4;
					while (j >= 0 && greed[j] == null) j--;
					return greed[j];
				}
				
				double max = -INF;
				PostureAndXY next = new PostureAndXY(-1, -1, -1, 0);
				
				for (PostureAndXY candicate : choices) {
					double scorce = strategy(map, b, candicate.o, candicate.x, candicate.y);
					if (max < scorce) {
						next.o = candicate.o;
						next.x = candicate.x;
						next.y = candicate.y;
						next.scorce = scorce;
						max = scorce;
					}
				}
				
				// �����ǰmap �Ĺ������ ���ڵ���3 �����÷�ģʽ
				
				learnRL = true;
				return next;
			}
		}
	}
	
	static int[][] clone(int[][] map){
		int n = map.length;
		int m = map[0].length;
		int[][] clone = new int[n][m];
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j) {
				clone[i][j] = map[i][j];
			}
		}
		return clone;
	}
	
	static int[] eliminate_scorce = {0, 1, 3, 5, 7};
	
		static boolean inValidPos(int[][] map, int b, int o, int x, int y) {
		if (b == 0 && o == 3) {
			MAP now = new MAP(clone(map));
			now.build(b, o, x, y);
			MAPINFO info = new MAPINFO(now.load());
			int e = info.calEliminate();
			eliminate(now.load());
			return e == 1 && now.map[20 - (y - 1)][x - 2] == 0;
		}
		
		if (b == 1 && o == 1) {
			MAP now = new MAP(clone(map));
			now.build(b, o, x, y);
			MAPINFO info = new MAPINFO(now.load());
			int e = info.calEliminate();
			eliminate(now.load());
			return e == 1 && now.map[20 - (y - 1)][x] == 0;
		}
		
		return false;
	}
	
	static boolean greedy(int[][] map, int b, int o, int x, int y, PostureAndXY[] greed) {
		MAP now = new MAP(clone(map));
		MAPINFO prev = new MAPINFO(now.load());
		prev.evaluate();
		
		now.build(b, o, x, y);
		MAPINFO next = new MAPINFO(now.load());
		int e = next.calEliminate();
		
		if (e == 0) return false;
		
		// �������ڵ���2 �Ҷ��ĸ���û�з����仯
		eliminate(now.load());
		
		next = new MAPINFO(now.load());
		next.evaluate();
		
		if (prev.hole - next.hole >= 0 && e >= 1) {
			greed[e] = new PostureAndXY(x, y, o, 0);
		}
		return prev.hole - next.hole >= 0 && e >= 1;
	}
	
	static boolean useStrategy3 = false;
	static boolean eliminate_one = false;
	
	static double strategy(int[][] map, int b, int o, int x, int y) {
		
		// ģ������
		MAP now = new MAP(clone(map));
		MAPINFO prev = new MAPINFO(now.load());
		prev.evaluate();
		prev.calSatiation();
		
		now.build(b, o, x, y);
		MAPINFO info = new MAPINFO(now.load());
		info.evaluate();
		info.fitEnviroment(b, o, x, y);
		
		
		BLOCKPOSINFO block_info = new BLOCKPOSINFO(b, o, x, y);
		return strategy_3(info, prev, block_info);
	}
	
	static class Pair{
		double scorce;
		int fit;
		
		Pair(double scorce, int fit){
			this.scorce = scorce;
			this.fit = fit;
		}
	}
	
	static Pair strategyToOppo(int[][] map, int b, int o, int x, int y) {
//		MAP now = new MAP(clone(map));
//		MAPINFO INFO = new MAPINFO(now.load());
//		int fit = INFO.fitEnviroment(b, o, x, y);
//		
//		now.build(b, o, x, y);
//		INFO = new MAPINFO(now.load());
//		int e = INFO.calEliminate();
//		
//		return fit;
		
		// ģ������
		MAP now = new MAP(clone(map));
		MAPINFO prev = new MAPINFO(now.load());
		prev.evaluate();
		prev.calSatiation();
		
		now.build(b, o, x, y);
		MAPINFO info = new MAPINFO(now.load());
		info.evaluate();
		int fit = info.fitEnviroment(b, o, x, y);
		
		BLOCKPOSINFO block_info = new BLOCKPOSINFO(b, o, x, y);
		return new Pair(strategy_3(info, prev, block_info), fit);
	}
	
	static double step_1(MAPINFO info, MAPINFO prev, BLOCKPOSINFO block) {
		// һ�� �� �Ͳ�����
		int e = info.calEliminate();
		int fit_env = info.fitEnviroment(block.b, block.o, block.x, block.y);
		
		if (e == 1 && (block.b == 1 && block.o == 1 || block.b == 0 && block.o == 3) && prev.nh <= 12) return -INF;
		
		// ���� ��������
		eliminate(info.map);
		info.evaluate();
		info.calBlockHeight(block.b, block.o, block.x, block.y);
		
		double scorce = 0;
		scorce -= info.nh * 100;
		scorce -= info.virtual_hole * 200;
		scorce -= info.valid_hole * 300; // ʵ��Խ��Խ��
		
		// ���ڵ���϶�
		scorce += fit_env * 300;
		scorce += 400 * eliminate_scorce[e];
		
		return scorce;
	}
	
	static PostureAndXY step_2(List<PostureAndXY> candicates, int[][] map, int b) {
		MAP now = new MAP(clone(map));
		
		double max = -1;
		PostureAndXY best = new PostureAndXY(-1, -1, -1, 0);
		
		// ���ȴ��ڵ���2��ƽ̨       �ߵ�̨ ���� ��һ��       �߶Ȳ�Ϊ2�ĸߵ�̨ ���� ��һ��
		for (PostureAndXY candicate : candicates) {
			int o = candicate.o;
			int x = candicate.x;
			int y = candicate.y;
			
			now.build(b, o, x, y);
			MAPINFO info = new MAPINFO(now.load());
			
			info.calPlatformInfo();
			if (max < info.gaoditai_1 + info.gaoditai_2 + info.platform) {
				max = info.gaoditai_1 + info.gaoditai_2 + info.platform;
				best.o = o;
				best.x = x;
				best.y = y;
			}
		}
		
		return best;
	}
	
	
	static int hole = 0;
	static double strategy_3(MAPINFO info, MAPINFO prev, BLOCKPOSINFO block) {
		int e = info.calEliminate();
		int fit_env = info.fitEnviroment(block.b, block.o, block.x, block.y);
		int deep = info.calDeep(block.b, block.o, block.x, block.y);
		int h_1 = prev.hole;
		
		// ���� ��������
		eliminate(info.map);
		info.evaluate();
		info.calBlockHeight(block.b, block.o, block.x, block.y);
		int h_2 = info.hole;
		
		hole = h_2 - h_1;
		
		// �ڵ�ǰ����·��Ƿ���� bottom Hole
		int bottomHole = prev.calBottomHole(block.b, block.o, block.x, block.y);
		
		double scorce = 0;
		
		scorce -= info.nh * (100 + info.nh * 5);
		scorce -= info.virtual_hole * (200 - info.nh * 5);
		scorce -= info.valid_hole * (300 - info.nh * 5); // ʵ��Խ��Խ��
		scorce -= info.block_height * (500 + info.nh * 5);
		
		scorce -= deep * (100 + info.nh * 5);
		scorce -= bottomHole * (100 - info.nh * 5);
		
		// ���ڵ���϶�
		scorce += fit_env * (300 + info.nh * 5);
		scorce += (400 + info.nh * 5) * eliminate_scorce[e] * (prev.satiation + 1);
		
//		System.out.println(block.b + " " + block.o + " " + block.x + " " + block.y);
//		System.out.println("��ǰ�߶ȣ�" + info.nh);
//		System.out.println("��ǰ������" + info.hole + " " + info.virtual_hole + " " + info.valid_hole);
//		System.out.println("��ǰ��ȣ�" + deep);
//		System.out.println("��ǰ�·��Ķ�����" + bottomHole);
//		System.out.println("��ǰ������϶ȣ�" + fit_env);
//		System.out.println("��ǰ��ȥ��������" + e);
//		System.out.println("������" + scorce);
		return scorce;
	}
	
	
	// �߶Ƚ�Сʱ���������� ��ƽ ��û�пն�
	static int strategy_1(MAPINFO info, MAPINFO prev, BLOCKPOSINFO block) {
		
		int e = info.calEliminate();
		int fit_env = info.fitEnviroment(block.b, block.o, block.x, block.y);
		int deep = info.calDeep(block.b, block.o, block.x, block.y);
		// ���� ��������
		eliminate(info.map);
		info.evaluate();
		
		// �ڵ�ǰ����·��Ƿ���� bottom Hole
		int bottomHole = prev.calBottomHole(block.b, block.o, block.x, block.y);
		
		int scorce = 0;
		scorce -= info.nh * 100;
		scorce -= info.hole * 100;  // û�ж�����õ� // �����
		scorce -= info.virtual_hole * 100;
		scorce -= info.valid_hole * 533; // ʵ��Խ��Խ��
		
		scorce -= deep * 233;
		
		scorce -= bottomHole * 50;
		
		// ���ڵ���϶�
		scorce += fit_env * 500;
		scorce += eliminate_scorce[e] * 400;
		
//		System.out.println(block.b + " " + block.o + " " + block.x + " " + block.y);
//		System.out.println("��ǰ�߶ȣ�" + info.nh);
//		System.out.println("��ǰ������" + info.hole + " " + info.virtual_hole + " " + info.valid_hole);
//		System.out.println("��ǰ��ȣ�" + deep);
//		System.out.println("��ǰ�·��Ķ�����" + bottomHole);
//		System.out.println("��ǰ������϶ȣ�" + fit_env);
//		System.out.println("��ǰ��ȥ��������" + e);
//		System.out.println("������" + scorce);
		return scorce;
	}
	
	// �߶Ƚϴ�ʱ���������� ���ٸ߶�  ���� �����߿�
	static int strategy_2(MAPINFO info, BLOCKPOSINFO block, MAPINFO prev) {
		int e = info.calEliminate();
		int fit_env = info.fitEnviroment(block.b, block.o, block.x, block.y);
		int deep = info.calDeep(block.b, block.o, block.x, block.y);
		// ���� ��������
		eliminate(info.map);
		info.evaluate();
		
		// �ڵ�ǰ����·��Ƿ���� bottom Hole
		int bottomHole = prev.calBottomHole(block.b, block.o, block.x, block.y);
		
		int scorce = 0;
		scorce -= info.hole * 100;  // û�ж�����õ� // �����
		scorce -= info.virtual_hole * 100;
		scorce -= info.valid_hole * 200; // ʵ��Խ��Խ��
		scorce -= block.bh * 533 / info.nh;   // �߶�Խ������ Խ��
		
		scorce -= deep * 233;
		scorce -= bottomHole * 50;
		scorce += fit_env * 300;
		scorce += eliminate_scorce[e] * (prev.satiation + 1) * info.nh * info.nh;
		
//		System.out.println(block.b + " " + block.o + " " + block.x + " " + block.y);
//		System.out.println("��ǰ�߶ȣ�" + info.nh);
//		System.out.println("��ǰ������" + info.hole + " " + info.virtual_hole + " " + info.valid_hole);
//		System.out.println("��ǰ��ȣ�" + deep);
//		System.out.println("��ǰ�·��Ķ�����" + bottomHole);
//		System.out.println("��ǰ������϶ȣ�" + fit_env);
//		System.out.println("��ǰ��ȥ��������" + e);
//		System.out.println("������" + scorce);
		return scorce;
	}
	
	static class Debug{
		
		static String println(int[][] map) {
			int n = map.length;
			int m = map[0].length;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < n; ++i) {
				for (int j = 0; j < m; ++j) {
					sb.append((map[i][j] == 1 ? '#' : '.') + (j + 1 == m ? "\n" : " "));
				}
			}
			System.out.println(sb.toString());
			return sb.toString();
		}
	}
	
	
	static void eliminate(int[][] map) {
		MAPINFO info = new MAPINFO(map);
		int e = info.calEliminate();
		if (e == 0) return;
		Set<Integer> range = info.range;
		
		int[][] aux = new int[20][10];
		for (int i = 19, t = 19; i >= 0; --i) {
			if (!range.contains(i)) {
				for (int j = 0; j < 10; ++j) {
					aux[t][j] = map[i][j];
				}
				t--;
			}
		}
		
		for (int i = 0; i < 20; ++i) {
			for (int j = 0; j < 10; ++j) {
				map[i][j] = aux[i][j];
			}
		}
	}
	
	static void add(int[][] map, int[][] row, Set<Integer> range) {
		int n = map.length;
		int m = map[0].length;
		int[][] clone = new int[n][m];
		
		int len = range.size();
		Integer[] ranges = range.toArray(new Integer[0]);
		Arrays.sort(ranges);
		
		for (int i = 0; i < n - len; ++i) {
			for (int j = 0; j < m; ++j) {
				clone[i][j] = map[i + len][j];
			}
		}
		
		int t = n - len;
		for (int i : ranges) {
			for (int j = 0; j < m; ++j) {
				clone[t][j] = row[i][j];
			}
			t ++;
		}
		
		// ��д
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j) {
				map[i][j] = clone[i][j];
			}
		}
	}
	
	// MAP[0] ��ʾ���� MAP[1] ��ʾ�Է�
	static MAP[] build(JSONArray req, JSONArray res) {
		MAP mine = new MAP();
		MAP oppo = new MAP();
		
		int n = res.size(); //���е�����
		if (n == 0) return new MAP[] {mine , oppo};
		
		// ��һ��
		int b1 = (int) ((Long) ((JSONObject) req.get(0)).get("block") - 0);
		int x1 = (int) ((Long) ((JSONObject) res.get(0)).get("x") - 0);
		int y1 = (int) ((Long) ((JSONObject) res.get(0)).get("y") - 0);
		int o1 = (int) ((Long) ((JSONObject) res.get(0)).get("o") - 0);
		
		mine.build(b1, o1, x1, y1);
		

		int b2 = (int) ((Long) ((JSONObject) req.get(0)).get("block") - 0);
		int x2 = (int) ((Long) ((JSONObject) req.get(1)).get("x") - 0);
		int y2 = (int) ((Long) ((JSONObject) req.get(1)).get("y") - 0);
		int o2 = (int) ((Long) ((JSONObject) req.get(1)).get("o") - 0);
		oppo.build(b2, o2, x2, y2);
	
		
		int myBouns = 0;
		int opBouns = 0;
		for (int i = 2; i <= n; ++i) {
			b2 = (int) ((Long) ((JSONObject) res.get(i - 2)).get("block") - 0);
			x2 = (int) ((Long) ((JSONObject) req.get(i)).get("x") - 0);
			y2 = (int) ((Long) ((JSONObject) req.get(i)).get("y") - 0);
			o2 = (int) ((Long) ((JSONObject) req.get(i)).get("o") - 0);
			
			int[][] prev2 = clone(oppo.load());
			oppo.build(b2, o2, x2, y2);
			
			b1 = (int) ((Long) ((JSONObject) req.get(i - 1)).get("block") - 0);
			x1 = (int) ((Long) ((JSONObject) res.get(i - 1)).get("x") - 0);
			y1 = (int) ((Long) ((JSONObject) res.get(i - 1)).get("y") - 0);
			o1 = (int) ((Long) ((JSONObject) res.get(i - 1)).get("o") - 0);
			
			int[][] prev1 = clone(mine.load());
			mine.build(b1, o1, x1, y1);
			
			MAPINFO info2 = new MAPINFO(oppo.load());
			int e2 = info2.calEliminate();
			
			if (e2  != 0) { // transfer
				opBouns ++;
				eliminate(oppo.load());
			}
			else {
				opBouns = 0;
			}
			
			MAPINFO info1 = new MAPINFO(mine.load());
			int e1 = info1.calEliminate();
			
			if (e1 != 0) {
				myBouns ++;
				eliminate(mine.load());
			}
			else {
				myBouns = 0;
			}
			
			if (e1 != 0) {
				add(oppo.load(), prev1, info1.range);
				// ��������
				if (myBouns >= 1000) {
					Set<Integer> lastRow = new HashSet<>();
					int maxRow = -1;
					for (int row : info1.range) {
						maxRow = Math.max(maxRow, row);
					}
					lastRow.add(maxRow);
					add(oppo.load(), prev1, lastRow);
				}
			}
			
			if (e2 != 0) { // transfer
				add(mine.load(), prev2, info2.range);
				// ��������
				if (opBouns >= 1000) {
					Set<Integer> lastRow = new HashSet<>();
					int maxRow = -1;
					for (int row : info2.range) {
						maxRow = Math.max(maxRow, row);
					}
					lastRow.add(maxRow);
					add(mine.load(), prev2, lastRow);
				}
			}
		}
		
		return new MAP[] {mine, oppo};
	}
	
	/******************************************************�۲���ֵ���Ϊ***********************************************************/
	static class MapGenerater {
		
		Map<Integer, Integer> cnt;
		
		boolean valid() {
			int max = Collections.max(cnt.values());
			int min = Collections.min(cnt.values());
			return max - min <= 2;
		}
		
		int nextBlock() {
			int block = 5;
			cnt.put(block, cnt.getOrDefault(block, 0) + 1);
			if (valid()) return block;
			else {
				cnt.put(block, cnt.getOrDefault(block, 0) - 1);
				return -1;
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
			// ��ʼ��
			for (int i = 0; i < BLOCK_NUM; ++i) {
				cnt.put(i, 0);
			}
		}
		
		MapGenerater(JSONArray req, JSONArray res){
			this();
			// ��� ��һ�غϵ�block��Ϣ
			int b = (int) ((Long) ((JSONObject) req.get(0)).get("block") - 0);
			cnt.put(b, cnt.getOrDefault(b, 0) + 1);
			
			int size = res.size();
			for (int i = 0; i < size; ++i) {
				b = (int) ((Long) ((JSONObject) res.get(i)).get("block") - 0);
				cnt.put(b, cnt.getOrDefault(b, 0) + 1);
			}
		}
		
		static void testBlock() {
			MapGenerater generater = new MapGenerater();
			for (int i = 0; i < 20; ++i) {
				System.out.println(generater.nextBlock());
			}
			System.out.println(generater.valid());
			System.out.println();
		}
	}
	
	static boolean isOppo = true;
	static int chooseWorstBlock(List<Integer> candicates, int[][] map) {
		int min_b = -1;
		double min_s = INF;
		for (int candicate : candicates) {
			if (candicate == 5) return 5; // ̰��
			PostureAndXY better = cal(map, candicate, isOppo); // ������϶�����
			double scorce = better.scorce;
			if (scorce < min_s) {
				min_s = scorce;
				min_b = candicate;
			}
		}
		return min_b;
	}
	
	static class Compress{
		
		static void read(String line, int[][] actions, int line_num) {
			actions[line_num / 7][line_num % 7] = Integer.parseInt(line);
		}
		
	}
	
	static final String FILE_NAME_10x5 = "data/OnlyHole_10x5.txt";
	static final int STATE_NUM_10x5  = 161051;  // 10 x 5  �Ķ���˹����
	static int[][] local_action_10x5 = new int[STATE_NUM_10x5][BLOCK_NUM];
	
	static final String FILE_NAME_7x5 = "data/NoHole_7x5.txt";
	static final int STATE_NUM_7x5 = 32768;
	static int[][] local_action_7x5 = new int[STATE_NUM_7x5][BLOCK_NUM];
	
	static void readData(String fileName, int[][] action) {
		DReader reader = new DReader(fileName);
		int line_num = 0;
		while (reader.hasNext()) {
			Compress.read(reader.next(), action, line_num);
			line_num ++;
		}
	}
	
	@SuppressWarnings({ "unchecked", "resource" })
	public static void main(String[] args) {
		if (D) {
		}
		else {
			// ���ݶ�д
			readData(FILE_NAME_10x5, local_action_10x5);
			readData(FILE_NAME_7x5, local_action_7x5);
			
			String input = new Scanner(System.in).nextLine();
			JSONObject inputJSON = (JSONObject) JSONValue.parse(input);
			
			JSONArray requests = (JSONArray) inputJSON.get("requests");
			JSONArray responses = (JSONArray) inputJSON.get("responses");
			int cnt = requests.size();
			
			MAP[] maps = build(requests, responses);
			
			// ������һ����Ϣ
			MAP info = maps[0];
			
			//�۲������Ϣ
			MAP oppo = maps[1];
			
			JSONObject obj = (JSONObject) requests.get(cnt - 1); //��õ�ǰ����Ҫ����Ϣ
			
			//���غϵ�block��
			Long block = (Long) obj.get("block");
			int  b = (int) (block - 0);
			
			// ������ʷ��Ϣ, �������λ�ú���̬
			int[][] map = info.load();
			PostureAndXY best = cal(map, b, false);  // ģ��Ծ�
			
			JSONObject outputJSON = new JSONObject();
			JSONObject resp = new JSONObject();
			
			// ����ѭ��, �۲����״̬
			MapGenerater g = new MapGenerater(requests, responses);
				// choose best candicates
			List<Integer> candicates = g.block_candicates();
			List<Integer> all = Arrays.asList(new Integer[] {0, 1, 2, 3, 4, 5, 6});
			int b_ = g.nextBlock();
			if (b_ == -1) {
				b_ = chooseWorstBlock(candicates, oppo.load());
			}
			
//			if (candicates.size() == 0) {
//				b_ = chooseWorstBlock(all, oppo.load());
//			}
//			else {
//				b_ = chooseWorstBlock(candicates, oppo.load());
//			}
			resp.put("block", b_); //���Է��Ŀ�
			
			// ����Ѳ���д��ϵͳ
			resp.put("x", best.x); 
			resp.put("y", best.y);
			resp.put("o", best.o); // �����̬
			
			outputJSON.put("response", resp);
			outputJSON.put("debug", learnRL);
			
			System.out.print(JSONValue.toJSONString(outputJSON));
		}
	}
}