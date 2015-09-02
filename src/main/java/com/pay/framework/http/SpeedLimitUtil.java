package com.pay.framework.http;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import com.pay.framework.util.ApplicationConfig;

/**
 * 
 * @author houzhaowei
 * 默认1秒200 次请求
 * 
 * @param <K>
 */
public class SpeedLimitUtil<K> {
	
	// 时间段内的次数
	private static int period = 1;
	// 时间段（秒）
	private static int times = 10000;

	private static SpeedLimitUtil<String> limit;
	
	private final HashMap<K, Record> rm = new HashMap<K, Record>();

	// 优先级queue
	private final PriorityQueue<Record> rq = new PriorityQueue<Record>(256,
			new Comparator<Record>() {
				public int compare(Record r1, Record r2) {
					return r1.deadline - r2.deadline;
				}
			});

	private SpeedLimitUtil() {
		String sPeriod = ApplicationConfig.get("speedlimit.period");
		String sTimes  = ApplicationConfig.get("speedlimit.times");
		if((null == sPeriod || "".equals(sPeriod)) || (null == sTimes || "".equals("times"))){
			return;
		}
		period = Integer.parseInt(sPeriod);
		times  = Integer.parseInt(sTimes);
	}
	
	public static SpeedLimitUtil<String> getInstance(){
		if(limit == null){
			limit = new SpeedLimitUtil<String>();
		}
		return limit;
	}

	private class Record {
		K key;
		int count = 1;
		int deadline = period + (int) (System.currentTimeMillis() / 1000);

		Record(K key) {
			this.key = key;
		}

		boolean isTimeout(int deadline) {
			return this.deadline <= deadline;
		}

		boolean update() {
			return count++ < times;
		}

		boolean isLimit() {
			return count < times;
		}
	}

	public synchronized boolean add(K key) {
		int now = (int) (System.currentTimeMillis() / 1000);
		Record r;
		// 移除
		for (; (r = rq.peek()) != null && r.isTimeout(now); rq.poll()) {
			rm.remove(r.key);
		}
		r = rm.get(key);
		if (r != null) {
			return r.update();
		}
		r = new Record(key);
		rq.add(r);
		rm.put(key, r);
		return true;
	}

	public synchronized boolean ask(K key) {
		int now = (int) (System.currentTimeMillis() / 1000);
		Record r;
		for (; (r = rq.peek()) != null && r.isTimeout(now); rq.poll()) {
			rm.remove(r.key);
		}
		r = rm.get(key);
		if (r != null) {
			return r.isLimit();
		}
		/*
		 * r = new Record( key ); rq.add( r ); rm.put( key, r );
		 */
		return true;
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		SpeedLimitUtil<String> sl = new SpeedLimitUtil<String>();

		System.out.println(sl.rm.size());
		System.out.println(sl.add("shit"));
		System.out.println(sl.add("shit"));
		System.out.println(sl.add("shit"));
		System.out.println(sl.add("shit"));
		System.out.println(sl.rm.size());
		System.out.println(sl.add("shit1"));
		System.out.println(sl.add("shit1"));
		System.out.println(sl.add("shit1"));
		System.out.println(sl.add("shit1"));
		System.out.println(sl.rm.size());
		Thread.sleep(3000);
		System.out.println(sl.add("shit1"));
		System.out.println(sl.add("shit1"));
		System.out.println(sl.add("shit1"));
		System.out.println(sl.add("shit1"));
		System.out.println(sl.rm.size());
	}
}