/* 
 * =============================================================
 * Copyright (C) 2007-2011 Edgenius (http://www.edgenius.com)
 * =============================================================
 * License Information: http://www.edgenius.com/licensing/edgenius/2.0/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2.0
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * http://www.gnu.org/licenses/gpl.txt
 *  
 * ****************************************************************
 */
package com.edgenius.core.util;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will run in independent thread to monitor threads to interrupt them in given timeout period.
 * 
 * This class life-cycle need be managed by Spring Frame(or others) by running start() and stop() method.
 *  
 * @author dapeng
 *
 */
public class ThreadInterruptManager {
	private static final Logger log = LoggerFactory.getLogger(ThreadInterruptManager.class);
	private static Queue<Container> pool = new ConcurrentLinkedQueue<Container>();
	//5 seconds
	private long checkInterval = 5;
	private static ReentrantLock lock = new ReentrantLock();
	private static Condition emptyCond = lock.newCondition();
	private Condition checkIntervalCond = lock.newCondition();
	private ExecutorService exec = Executors.newSingleThreadExecutor();
	private boolean shutdown = false;
	/**
	 * This class initialize method called by Spring framework.
	 */
	public void start(){
		Monitor monitor = new Monitor();
		exec.submit(monitor);
	}
	/**
	 * This class destroy method called by Spring framework.
	 */

	public void stop(){
		shutdown = true;
		try{
			lock.lock();
			//something coming, then ask monitor start
			emptyCond.signalAll();
		}finally{
			lock.unlock();
		}
		try{
			lock.lock();
			checkIntervalCond.signalAll();
		}finally{
			lock.unlock();
		}
		exec.shutdownNow();
	}
	
	/**
	 * Add a thread to monitor. If timeout is achieve, this thread will be interrupt. The precious is dependent
	 * on <code>checkInterval</code>.
	 * 
	 * @param thread
	 * @param timeout unit is second
	 */
	public static void addThread(Thread thread, int timeout){
		pool.add(new Container(thread,timeout));
		try{
			lock.lock();
			//something coming, then ask monitor start
			emptyCond.signalAll();
		}finally{
			lock.unlock();
		}
		
	}
	/**
	 * Try to remove given thread from Monitor pool.
	 * @param thread
	 */
	public static void removeThread(Thread thread){
		for(Iterator<Container> iter = pool.iterator();iter.hasNext();){
			Container con = iter.next();
			if(con.thread.equals(thread)){
				iter.remove();
				log.info("Thread " + thread.getName() + " success removed from Monitor pool");
				break;
			}
		}
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}
	//********************************************************************
	//               private class
	//********************************************************************
	private class Monitor implements Callable<Boolean>{

		public Boolean call() throws Exception {
			Thread.currentThread().setName("Thread Interrupt Manager Monitor");
			while(true){
				if(shutdown)
					break;
				long end = System.currentTimeMillis();
				for (Iterator<Container> iter = pool.iterator();iter.hasNext();) {
					Container con = iter.next();
					long  dur = end - con.start;
					if(dur > con.timeout){
						con.thread.interrupt();
						iter.remove();
					}
				}
				log.info("Pool size is " + pool.size());
				if(pool.isEmpty()){
					try{
						log.info("pool empty waiting...");
						lock.lock();
						emptyCond.await();
					} catch (InterruptedException e) {
						log.info("Monitor thread Interrupted :" + e);
					}finally{
						lock.unlock();
					}
				}
				if(shutdown)
					break;
				try{
					lock.lock();
					checkIntervalCond.await(checkInterval, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					log.error("Monitor thread Interrupted :" , e);
				}finally{
					lock.unlock();
				}
			}
			
			return true;
		}
	}

	private static class Container{
		public Thread thread;
		public long timeout;
		public long start;
		public Container(Thread thread, int timeout){
			this.thread = thread;
			this.timeout = timeout * 1000;
			this.start = System.currentTimeMillis();
		}
	}

}
