/*
 * Copyright (C) 2000-2013 Heinz Max Kabutz
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Heinz Max Kabutz licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.javaspecialists.tjsn.examples.issue184;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * This caused a deadlock prior to Java 7 on Sun/Oracle JVMs
 */
public class VectorSerializationDeadlock {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(2);

        final Vector[] vecs = {
                new Vector(),
                new Vector(),
        };
        vecs[0].add(vecs[1]);
        vecs[1].add(vecs[0]);

        for (int i = 0; i < 2; i++) {
            final int threadNumber = i;
            pool.submit(new Callable() {
                public Object call() throws Exception {
                    for (int i = 0; i < 1000 * 1000; i++) {
                        ObjectOutputStream out = new ObjectOutputStream(
                                new NullOutputStream()
                        );
                        out.writeObject(vecs[threadNumber]);
                        out.close();
                    }
                    System.out.println("done");
                    return null;
                }
            });
        }
        pool.shutdown();
        while (!pool.awaitTermination(1, TimeUnit.SECONDS)) {
            System.out.println("Pool not shut down yet.");
        }
    }
}

