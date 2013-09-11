package edu.unh.schwartz.parawrap.worker;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;

/**
 * Worker. Blah blah blah
 */
public final class Worker extends Thread
{
    /**
     * The queue of pieces to do work on.
     */
    private PriorityBlockingQueue<String> queue;
    
    /**
     * The id number of this worker.
     */
    private int idNum;
    
    /**
     * The sum of run times of executable.
     */
    private long runTime;

    /**
     * Creates a worker.
     *
     * @param idNum - the id number of this worker
     * @param queue - the queue for the worker to take chunks from
     */
    public Worker(final int idNum, final PriorityBlockingQueue<String> queue)
    {
        this.queue = queue;
        this.idNum = idNum;
        this.runTime = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run()
    {
        // Make output file for this thread
        final String outDir = "/tmp/kaks" + this.idNum;
        final File dir = new File(outDir);
        dir.mkdirs();

        int i = 0;
        while (this.queue.size() != 0)
        {
            final String s = this.queue.poll();

            // Make temporary files
            final String inName = outDir + "/temp.axt";
            final File in = new File(inName);
            final String outName = outDir + "/result" + i + ".kaks";
            final File out = new File(outName);
            try
            {
                in.createNewFile();
                
                final PrintWriter temp = new PrintWriter(in);
                temp.println(s);
                temp.close();

                out.createNewFile();
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
            }
 
            try
            {
                final ProcessBuilder pb = createProcess(inName, outName);
                pb.redirectErrorStream(true);

                System.out.println("Starting chunk on thread " + this.idNum);
                final long start = System.currentTimeMillis();
                final Process proc = pb.start();
                proc.waitFor();
                final long end = System.currentTimeMillis();
                runTime += (end - start);
                System.out.println("Finished chunk on thread " + this.idNum);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                System.err.println("Exec error");
                e.printStackTrace();
            }

            i++;
        }
    }

    private ProcessBuilder createProcess(final String in, final String out)
    {
        final List<String> commands = new ArrayList<String>();
        commands.add("./KaKs_Calculator");
        commands.add("-i");
        commands.add(in);
        commands.add("-o");
        commands.add(out);
 
        return new ProcessBuilder(commands);
    }

    /**
     * @return the total run time of the executable of this worker
     */
    public long getRunTime()
    {
        return this.runTime;
    }
}
