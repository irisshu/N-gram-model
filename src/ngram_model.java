import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Map;
import java.util.Scanner;
//符號對應
//Count(Wi) : uniMap.get(parts[i])
//Count(Wi,Wi+1) : biMap.get(pair(parts[i],parts[i+1]))

public class ngram_model {

	static String user_parts[] = null;
	final static int k = 10;
	final static int V = 80000;
	static Map<String, Integer> uniMap = new HashMap<String, Integer>();
	static Map<String, Integer> biMap = new HashMap<String, Integer>();
	static double P = 0;
	static double N_u_0 = 0;
	static double N_b_0 = 0;

	public static void main(String[] args) throws Exception {

		// Get user input
		System.out.println("Please input a sentence:");
		Scanner scanner = new Scanner(System.in);

		String user_line = scanner.nextLine();
		System.out.println("第一次計算較久...");
		System.out.print("P*('" + user_line + "') : ");
		String low_user_line = user_line.toLowerCase();
		user_parts = low_user_line.split(" ");

		// Get training data from txt file.
		BufferedReader in = new BufferedReader(new FileReader("dataset.txt"));
		String data_line = "";
		String data_line2 = "";

		while ((data_line = in.readLine()) != null) {
			data_line2 = data_line2 + "" + data_line;
		}

		String low_data_line = data_line2.toLowerCase();
		String parts[] = low_data_line.split(" ");

		for (int i = 0; i < parts.length; i++) {
			// 先都初始化為0次
			uniMap.put(parts[i], 0);
			if (i == parts.length - 1) {
				// 不做事
			} else {// 變成bigram
				biMap.put(pair(parts[i], parts[i + 1]), 0);
			}
		}

		for (int i = 0; i < parts.length; i++) {
			// 將各單字對應，計算出現次數
			uniMap.put(parts[i], uniMap.get(parts[i]) + 1);
			// System.out.println(parts[i] + " " + uniMap.get(parts[i]));

			if (i == parts.length - 1) {
				// 不做事
			} else {// 變成bigram
				biMap.put(pair(parts[i], parts[i + 1]),
						biMap.get(pair(parts[i], parts[i + 1])) + 1);

			}
		}
		//System.out.println("uniMap.size() " + uniMap.size());
		prepare();
		process();
		// probability();

		// Get user input
		System.out.println("Please input a sentence:");
		while (scanner.hasNext()) {
			user_line = scanner.nextLine();
			System.out.print("P*('" + user_line + "') : ");
			low_user_line = user_line.toLowerCase();
			user_parts = low_user_line.split(" ");
			process();
		}

	}

	public static String pair(String first, String second) {
		return first + " " + second;
	}

	static Map<Integer, Integer> Nc_u_Map = new HashMap<Integer, Integer>();
	static Map<Integer, Integer> Nc_b_Map = new HashMap<Integer, Integer>();

	public static void prepare() {
		// . you , you (, you) ++
		// uniMap frequency counting
		Collection temp = uniMap.values();
		Object[] uni = temp.toArray();
		for (int i = 0; i < uniMap.size(); i++) { // 先都初始化為0次
			Nc_u_Map.put((Integer) uni[i], 0);
		}

		for (int i = 0; i < uniMap.size(); i++) {
			Nc_u_Map.put((Integer) uni[i], Nc_u_Map.get(uni[i]) + 1);
			// System.out.println(uni[i] +" " + Nc_u_Map.get(uni[i]) );
		}

		// biMap frequency counting
		Collection temp_bi = biMap.values();
		Object[] bi = temp_bi.toArray();
		for (int i = 0; i < biMap.size(); i++) {
			// 先都初始化為0次
			Nc_b_Map.put((Integer) bi[i], 0);
		}

		for (int i = 0; i < biMap.size(); i++) {
			Nc_b_Map.put((Integer) bi[i], Nc_b_Map.get(bi[i]) + 1);

		}

		// 另外計算 N_u_0
		for (int j = 0; j < uniMap.size(); j++) {
			if (Nc_u_Map.get(j + 1) == null) {
				// 超出範圍就不做事
			} else {
				N_u_0 = N_u_0 + Nc_u_Map.get(j + 1);
			}

		}
		N_u_0 = V - N_u_0;

		// 另外計算 N_b_0
		for (int j = 0; j < biMap.size(); j++) {
			if (Nc_b_Map.get(j + 1) == null) {
				// 超出範圍就不做事
			} else {
				N_b_0 = N_b_0 + (Nc_b_Map.get(j + 1));
			}

		}
		N_b_0 = V * V - N_b_0;
		//System.out.println(" N_b_0 " + N_b_0);
	}

	public static double getCount_star_wi(String wi) {
		int Count_wi = getCount_wi(wi);
		if (Count_wi < k) {
			return (Count_wi + 1) * (getNc_u(Count_wi + 1) / getNc_u(Count_wi));
		} else {
			return Count_wi;
		}
	}

	public static double getNc_u(int n) {
		if (Nc_u_Map.containsKey(n))
			return Nc_u_Map.get(n);
		return 0;
	}

	public static int getCount_wi(String wi) {
		if (uniMap.containsKey(wi))
			return uniMap.get(wi);
		return 0;
	}

	public static double getCount_star_wi_wi1(String wi, String wi1) {
		int Count_wi_wi1 = getCount_wi_wi1(wi, wi1);
		if (Count_wi_wi1 < k) {
			return (Count_wi_wi1 + 1)
					* (getNc_b(Count_wi_wi1 + 1) / getNc_b(Count_wi_wi1));
		} else {
			return Count_wi_wi1;
		}
	}

	public static double getNc_b(int n) {
		if (Nc_b_Map.containsKey(n))
			return Nc_b_Map.get(n);
		return 0;
	}

	public static int getCount_wi_wi1(String wi, String wi1) {
		if (biMap.containsKey(pair(wi, wi1)))
			return biMap.get(pair(wi, wi1));
		return 0;
	}

	public static double getP_wi(String wi) {
		double Count_wi = getCount_wi(wi);
		if (Count_wi == 0) {
			return (double) Nc_u_Map.get(1) / (N_u_0 * uniMap.size());
			// System.out.println(" P_wi[i] " + P_wi[i]);
		} else if (0 < Count_wi && Count_wi < k) {
			return (double) getCount_star_wi(wi) / uniMap.size();
			// System.out.println(" P_wi[i] " + P_wi[i]);
		} else {
			return (double) Count_wi / uniMap.size();
			// System.out.println(" P_wi[i] " + P_wi[i]);
		}
	}

	public static double getP_wi_wi1(String wi, String wi1) {
		double Count_wi_wi1 = getCount_wi_wi1(wi, wi1);
		if (Count_wi_wi1 == 0) {
			return (double) Nc_b_Map.get(1) / (N_b_0 * biMap.size());
			// System.out.println(" P_wi[i] " + P_wi[i]);
		} else if (0 < Count_wi_wi1 && Count_wi_wi1 < k) {
			return (double) getCount_star_wi_wi1(wi, wi1) / biMap.size();
			// System.out.println(" P_wi[i] " + P_wi[i]);
		} else {
			return (double) Count_wi_wi1 / biMap.size();
			// System.out.println(" P_wi[i] " + P_wi[i]);
		}
	}

	public static void process() {
		P = 1;
		for (int i = 0; i < user_parts.length; i++) {
			if (i == 0) {
				P *= getP_wi(user_parts[i]);
			} else {
				P *= getP_wi_wi1(user_parts[i - 1], user_parts[i])
						/ getP_wi(user_parts[i - 1]);
				// Ps *= P(user_parts[i] | user_parts[i-1]);
			}
		}
		System.out.printf("P(s) = %e\n", P);
	}

	// public static double probability() {
	//
	// // Calculate count*(Wi)
	// double count_star_wi[] = new double[user_parts.length];
	// double count_star_wi_wi1[] = new double[user_parts.length];
	// double Count_wi = 0;
	// double Count_wi_wi1 = 0;
	//
	// P = 1;
	// for (int i = 0; i < user_parts.length; i++) {
	// if (i == 0) {
	// P *= getP_wi(user_parts[i]);
	// } else {
	// P *= getP_wi_wi1(user_parts[i - 1], user_parts[i])
	// / getP_wi(user_parts[i - 1]);
	// // Ps *= P(user_parts[i] | user_parts[i-1]);
	// }
	// // System.out.println(user_parts[i]);
	// Count_wi = getCount_wi(user_parts[i]);
	// if (Count_wi < k) {
	//
	// if (Nc_u_Map.get(Count_wi + 1) == null) {
	// System.out.println(Nc_u_Map.get(Count_wi + 1));
	// count_star_wi[i] = 1;// 暫時改
	// } else {
	// count_star_wi[i] = (Count_wi + 1.0)
	// * Nc_u_Map.get(Count_wi + 1)
	// / Nc_u_Map.get(Count_wi);
	// }
	//
	// } else if (Count_wi >= k) {
	// count_star_wi[i] = Count_wi;
	// }
	//
	// // bigram 處理
	// if (i < user_parts.length - 1) {
	// if (biMap.containsKey(pair(user_parts[i], user_parts[i + 1])))
	// Count_wi_wi1 = (Integer) biMap.get(pair(user_parts[i],
	// user_parts[i + 1]));
	// else
	// Count_wi_wi1 = 0;
	// if (Count_wi_wi1 < k) {
	//
	// count_star_wi_wi1[i] = (Count_wi_wi1 + 1.0)
	// * Nc_b_Map.get(Count_wi_wi1 + 1)
	// / Nc_b_Map.get(Count_wi_wi1);
	//
	// } else if (Count_wi_wi1 >= k) {
	// count_star_wi_wi1[i] = Count_wi_wi1;
	// }
	// }
	// System.out.println("count_star_wi " + count_star_wi[i]);
	// System.out.println("count_star_wi_wi1 " + count_star_wi_wi1[i]);
	//
	// // Calculate probabilities
	// double P_wi[] = new double[user_parts.length];
	// double P_wi_wi1[] = new double[user_parts.length];
	//
	// System.out.println(" N_u_0 " + N_u_0);
	//
	// if (Count_wi == 0) {
	// P_wi[i] = Nc_u_Map.get(1) / N_u_0 * uniMap.size();
	// System.out.println(" P_wi[i] " + P_wi[i]);
	// } else if (0 < Count_wi && Count_wi < k) {
	// P_wi[i] = count_star_wi[i] / uniMap.size();
	// System.out.println(" P_wi[i] " + P_wi[i]);
	// } else if (Count_wi >= k) {
	// P_wi[i] = Count_wi / uniMap.size();
	// System.out.println(" P_wi[i] " + P_wi[i]);
	// }
	//
	// if (Count_wi_wi1 == 0) {
	// P_wi_wi1[i] = Nc_b_Map.get(1) / N_b_0 * biMap.size();
	// System.out.println("Count_wi_wi1 == 0");
	// } else if (0 < Count_wi_wi1 && Count_wi_wi1 < k) {
	// P_wi_wi1[i] = count_star_wi_wi1[i] / biMap.size();
	// System.out.println("P_wi_wi1[i]" + P_wi_wi1[i]);
	// } else if (Count_wi_wi1 >= k) {
	// P_wi_wi1[i] = Count_wi_wi1 / biMap.size();
	// System.out.println("P_wi_wi1[i]" + P_wi_wi1[i]);
	// }
	// if (i == 0)
	// P *= P_wi[i]; // P(s) = P(w0)
	// else
	// P *= (P_wi_wi1[i] / P_wi[i]); // P(s) *= P(Wi+1|W) = P(Wi+1,
	// // wi)/P(wi)
	// // P= P_wi[i];
	// }
	//
	// System.out.printf("P %e \n", P);
	// return P;
	//
	// }

}
