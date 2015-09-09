/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.rrd.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * The Abstract RRD (Round Robin Database).
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class AbstractRRD {

    /** The version of the RRD Dump. */
    private String version;

    /** The step (interval) expressed in seconds. */
    private Long step;

    /** The last update time stamp, expressed in seconds since 1970-01-01 UTC. */
    private Long lastupdate;

    /**
     * Creates the RRD.
     *
     * @return the abstract RRD
     */
    protected abstract AbstractRRD createRRD();

    /**
     * Gets the data sources.
     *
     * @return the data sources
     */
    public abstract List<? extends AbstractDS> getDataSources();

    /**
     * Gets the RRAs.
     *
     * @return the RRAs
     */
    public abstract List<? extends AbstractRRA> getRras();

    /**
     * Adds the RRA.
     *
     * @param rra the RRA
     */
    public abstract void addRRA(AbstractRRA rra);

    /**
     * Adds the data source.
     *
     * @param ds the DS
     */
    public abstract void addDataSource(AbstractDS ds);

    /**
     * Gets the data source.
     *
     * @param index the index
     * @return the data source
     */
    public abstract AbstractDS getDataSource(int index);

    /**
     * Gets the version of the RRD Dump.
     *
     * @return the version
     */
    @XmlElement(name="version")
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the step (interval) expressed in seconds.
     *
     * @return the step
     */
    @XmlElement(name="step")
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getStep() {
        return step;
    }

    /**
     * Sets the step.
     *
     * @param step the new step
     */
    public void setStep(Long step) {
        this.step = step;
    }

    /**
     * Gets the last update.
     *
     * @return the last update
     */
    @XmlElement(name="lastupdate")
    @XmlJavaTypeAdapter(LongAdapter.class)
    public Long getLastUpdate() {
        return lastupdate;
    }

    /**
     * Sets the last update.
     *
     * @param lastUpdate the new last update
     */
    public void setLastUpdate(Long lastUpdate) {
        this.lastupdate = lastUpdate;
    }

    /**
     * Gets the start time stamp, expressed in seconds since 1970-01-01 UTC.
     *
     * @param rra the RRA
     * @return the start time stamp (in seconds)
     */
    public Long getStartTimestamp(AbstractRRA rra) {
        if (getLastUpdate() == null || getStep() == null || rra == null) {
            return null;
        }
        return getEndTimestamp(rra) - getStep() * rra.getPdpPerRow() * (rra.getRows().size() - 1);
    }

    /**
     * Gets the end time stamp, expressed in seconds since 1970-01-01 UTC.
     *
     * @param rra the RRA
     * @return the end time stamp (in seconds)
     */
    public Long getEndTimestamp(AbstractRRA rra) {
        if (getLastUpdate() == null || getStep() == null || rra == null) {
            return null;
        }
        return getLastUpdate() - getLastUpdate() % (getStep() * rra.getPdpPerRow());
    }

    /**
     * Finds the row time stamp, expressed in seconds since 1970-01-01 UTC.
     *
     * @param rra the RRA object
     * @param row the Row object
     * @return the long
     */
    public Long findTimestampByRow(AbstractRRA rra, Row row) {
        int rowNumber = rra.getRows().indexOf(row);
        if (rowNumber < 0) {
            return null;
        }
        return getStartTimestamp(rra) + rowNumber * rra.getPdpPerRow() * getStep();
    }

    /**
     * Gets the row that corresponds to a specific time stamp (expressed in seconds since 1970-01-01 UTC).
     *
     * @param rra the RRA
     * @param timestamp the row time stamp
     * @return the row object
     */
    public Row findRowByTimestamp(AbstractRRA rra, Long timestamp) {
        try {
            Long n = (timestamp - getStartTimestamp(rra)) / (rra.getPdpPerRow() * getStep());
            return rra.getRows().get(n.intValue());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Merge.
     * <p>Merge the content of rrdSrc into this RRD.</p>
     * <p>The format must be equal in order to perform the merge operation.</p>
     * 
     * @param rrdSrc the RRD source
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void merge(AbstractRRD rrdSrc) throws IllegalArgumentException {
        if (!hasMergeableRRAs(rrdSrc)) {
            throw new IllegalArgumentException("Invalid RRD format. There are not mergeable RRAs on the source RRD.");
        }
        for (AbstractRRA rra : rrdSrc.getRras()) {
            AbstractRRA localRra = getMergeableRRA(rra);
            if (localRra != null) {
                for (Row row : rra.getRows()) {
                    if (!row.isNan()) {
                        Long ts = rrdSrc.findTimestampByRow(rra, row);
                        Row localRow = findRowByTimestamp(localRra, ts);
                        if (localRow != null) {
                            localRow.setValues(row.getValues());
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks for mergeable RRAs.
     *
     * @param rrdSrc the RRD source
     * @return true, if there are mergeable RRAs
     */
    public boolean hasMergeableRRAs(AbstractRRD rrdSrc) {
        for (AbstractRRA rraSrc : rrdSrc.getRras()) {
            for (AbstractRRA rra : getRras()) {
                if (rra.formatMergeable(rraSrc)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the mergeable RRA.
     *
     * @param rraSrc the source RRA
     * @return the local RRA mergeable with rraSrc
     */
    public AbstractRRA getMergeableRRA(AbstractRRA rraSrc) {
        for (AbstractRRA rra : getRras()) {
            if (rra.formatMergeable(rraSrc)) {
                return rra;
            }
        }
        return null;
    }

    /**
     * Merge.
     *
     * @param rrdList the RRD list
     * @throws IllegalArgumentException the illegal argument exception
     */
    public void merge(List<? extends AbstractRRD> rrdList) throws IllegalArgumentException {
        if (rrdList.size() != getDataSources().size()) {
            final String msg = String.format("Cannot merge RRDs because the amount of RRDs (%d) doesn't match the amount of data sources (%d)", rrdList.size(), getDataSources().size());
            throw new IllegalArgumentException(msg);
        }
        int validDsFound = 0;
        for (AbstractRRD singleMetricRrd : rrdList) {
            if (!singleMetricRrd.getStep().equals(getStep())) {
                throw new IllegalArgumentException("Cannot merge RRDs because one of them have a different step value.");
            }
            if (!singleMetricRrd.getVersion().equals(getVersion())) {
                throw new IllegalArgumentException("Cannot merge RRDs because one of them have a different file version.");
            }
            if (singleMetricRrd.getDataSources().size() > 1) {
                throw new IllegalArgumentException("Cannot merge RRDs because one of them has more than one DS.");
            }
            for (AbstractDS ds : getDataSources()) {
                if (ds.getName().equals(singleMetricRrd.getDataSource(0).getName())) {
                    validDsFound++;
                    break;
                }
            }
            if (!hasMergeableRRAs(singleMetricRrd)) {
                throw new IllegalArgumentException("Cannot merge RRDs because there are no mergeable RRA configuration.");
            }
        }
        if (validDsFound != getDataSources().size()) {
            throw new IllegalArgumentException("Cannot merge RRDs because some data sources don't have a RRD file on the list.");
        }
        for (AbstractRRA localRra : getRras()) {
            for (Row localRow : localRra.getRows()) {
                for (int k = 0; k < getDataSources().size(); k++) {
                    final String ds = getDataSources().get(k).getName();
                    AbstractRRA singleMetricRra = null;
                    AbstractRRD singleMetricRrd = null;
                    for (AbstractRRD rrd : rrdList) {
                        if (rrd.getDataSource(0).getName().equals(ds)) {
                            singleMetricRrd = rrd;
                            singleMetricRra = rrd.getMergeableRRA(localRra);
                            break;
                        }
                    }
                    if (singleMetricRra != null) {
                        Long ts = findTimestampByRow(localRra, localRow);
                        Row row = singleMetricRrd.findRowByTimestamp(singleMetricRra, ts);
                        if (row != null) {
                            Double v = row.getValues().get(0);
                            if (!v.isNaN()) {
                                localRow.getValues().set(k, v);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Split.
     * <p>If the RRD contain several data sources, it will return one RRD per DS.
     * Otherwise, it will throw an exception.</p>
     *
     * @return the RRD list
     * @throws IllegalArgumentException the illegal argument exception
     */
    public List<AbstractRRD> split() throws IllegalArgumentException {
        if (getDataSources().size() <= 1) {
            throw new IllegalArgumentException("Cannot split an RRD composed by 1 or less data-sources.");
        }
        List<AbstractRRD> rrds = new ArrayList<AbstractRRD>();
        for (int i = 0; i < getDataSources().size(); i++) {
            AbstractRRD rrd = createRRD();
            rrd.addDataSource(getDataSource(i));
            for (int j = 0; j < getRras().size(); j++) {
                AbstractRRA currentRra = getRras().get(j);
                AbstractRRA rra = currentRra.createSingleRRA(i);
                for (Row currentRow : currentRra.getRows()) {
                    Row row = new Row();
                    row.getValues().add(currentRow.getValues().get(i));
                    rra.getRows().add(row);
                }
                rrd.addRRA(rra);
            }
            rrds.add(rrd);
        }
        return rrds;
    }

    /**
     * Format equals.
     *
     * @param rrd the RRD object
     * @return true, if successful
     */
    public boolean formatEquals(AbstractRRD rrd) {
        if (this.step != null) {
            if (rrd.step == null) return false;
            else if (!(this.step.equals(rrd.step))) 
                return false;
        }
        else if (rrd.step != null)
            return false;

        if (this.getDataSources() != null) {
            if (rrd.getDataSources() == null) return false;
            else if (!(this.getDataSources().size() == rrd.getDataSources().size())) 
                return false;
        }
        else if (rrd.getDataSources() != null)
            return false;

        for (int i = 0; i < getDataSources().size(); i++) {
            if (!getDataSources().get(i).formatEquals(rrd.getDataSources().get(i)))
                return false;
        }

        if (!hasEqualsRras(rrd)) {
            return false;
        }

        return true;
    }

    /**
     * Checks for equals RRAs.
     *
     * @param rrd the RRD object
     * @return true, if successful
     */
    public boolean hasEqualsRras(AbstractRRD rrd) {
        if (this.getRras() != null) {
            if (rrd.getRras() == null) return false;
            else if (!(this.getRras().size() == rrd.getRras().size())) 
                return false;
        }
        else if (rrd.getRras() != null)
            return false;

        for (int i = 0; i < getRras().size(); i++) {
            if (!getRras().get(i).formatEquals(rrd.getRras().get(i)))
                return false;
        }

        return true;
    }

    /**
     * Generate raw samples.
     *
     * @return the all samples
     */
    @XmlTransient
    public List<Sample> generateSamples() {
        List<Sample> samples = new ArrayList<Sample>();
        getRras().stream().filter(r -> r.hasAverageAsCF()).sorted((r1,r2) -> r1.getPdpPerRow().compareTo(r2.getPdpPerRow())).forEach(r -> {
            generateSamples(r).forEach(s -> {
                if (!samples.contains(s))
                    samples.add(s);
            });
        });
        Collections.sort(samples);
        return samples;
    }

    /**
     * Resets the row values for all the RRAs.
     * <p>Double.NaN will be stored on each slot</p>
     */
    @XmlTransient
    public void reset() {
        getRras().stream().flatMap(rra -> rra.getRows().stream()).forEach(row -> {
            List<Double> values = new ArrayList<Double>();
            row.getValues().forEach(d -> values.add(Double.NaN));
            row.setValues(values);
        });
    }

    /**
     * Generate raw samples.
     *
     * @param rra the source RRA to be used (it must have AVERAGE for its consolidation function)
     * @return the samples for the given RRA
     */
    @XmlTransient
    public List<Sample> generateSamples(AbstractRRA rra) {
        List<Sample> samples = new ArrayList<Sample>();
        if (!rra.hasAverageAsCF()) {
            return samples;
        }

        long step = rra.getPdpPerRow() * getStep();
        long start = getStartTimestamp(rra);
        long end = getEndTimestamp(rra);

        // Initialize Values Map
        Map<Long,List<Double>> valuesMap = new TreeMap<Long,List<Double>>();
        for (long ts = start; ts <= end; ts+= step) {
            List<Double> values = new ArrayList<Double>();
            for (int i=0; i<getDataSources().size(); i++) {
                values.add(Double.NaN);
            }
            valuesMap.put(new Long(ts), values);
        }

        // Initialize Last Values
        List<Double> lastValues = new ArrayList<Double>();
        for (AbstractDS ds : getDataSources()) {
            Double v = ds.getLastDs() == null ? 0.0 : ds.getLastDs();
            lastValues.add(v - v % step);
        }

        // Set Last-Value for Counters
        for (int i = 0; i < getDataSources().size(); i++) {
            if (getDataSource(i).isCounter()) {
                valuesMap.get(new Long(end)).set(i, lastValues.get(i));
            }
        }

        // Process
        long ts = end - step;
        for (int j = rra.getRows().size() - 1; j >= 0; j--) {
            Row counterSrc = rra.getRows().get(j);
            for (int i = 0; i < getDataSources().size(); i++) {
                if (getDataSource(i).isCounter()) {
                    if (j > 0) {
                        Double last = lastValues.get(i);
                        Double current = counterSrc.getValue(i).isNaN() ? 0 : counterSrc.getValue(i);
                        Double value = last - current * step;
                        lastValues.set(i, value);
                        if (!counterSrc.getValue(i).isNaN()) {
                            valuesMap.get(new Long(ts)).set(i, value);
                        }
                    }
                } else {
                    if (!counterSrc.getValue(i).isNaN()) {
                        valuesMap.get(new Long(ts + step)).set(i, counterSrc.getValue(i));
                    }
                }
            }
            ts -= step;
        }

        // Update Samples
        valuesMap.forEach((timestamp, data) -> samples.add(new Sample(timestamp,data)));
        return samples;
    }

    /**
     * Gets the index.
     *
     * @param dsName the DS name
     * @return the index
     */
    protected int getIndex(String dsName) {
        if (getDataSources() == null) {
            return -1;
        }
        for (int i=0; i < getDataSources().size(); i++) {
            if (getDataSources().get(i).getName().equals(dsName)) {
                return i;
            }
        }
        return -1;
    }

}
