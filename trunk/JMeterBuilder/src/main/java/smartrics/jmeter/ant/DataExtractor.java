/*  Copyright 2009 Fabrizio Cannizzo
 *
 *  This file is part of JMeterRestSampler.
 *
 *  JMeterRestSampler (http://code.google.com/p/rest-fixture/) is free software:
 *  you can redistribute it and/or modify it under the terms of the
 *  GNU Lesser General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *
 *  JMeterRestSampler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JMeterRestSampler.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  If you want to contact the author please see http://smartrics.blogspot.com
 */
package smartrics.jmeter.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.save.TestResultWrapper;

public abstract class DataExtractor {

    private File jtlFile;

    public DataExtractor(File jtlFile) {
        super();
        if (jtlFile == null) {
            throw new IllegalArgumentException("jtl file is null");
        }
        this.jtlFile = jtlFile;
    }

    public File getJtlFile() {
        return jtlFile;
    }

    @SuppressWarnings("unchecked")
    public void handleResults() {
        String jtlFilePath = jtlFile.getAbsolutePath();
        try {
            FileInputStream fis = new FileInputStream(getJtlFile());
            TestResultWrapper w = SaveService.loadTestResults(fis);
            Collection<SampleResult> results = w.getSampleResults();
            for (SampleResult r : results) {
                try {
                    handle(r);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("jtl file '" + jtlFilePath + "' contains data not processable", e);
                }
            }
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Invalid input file " + jtlFilePath, e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to load test results from " + jtlFilePath, e);
        }
    }

    public abstract void handle(SampleResult res);

}