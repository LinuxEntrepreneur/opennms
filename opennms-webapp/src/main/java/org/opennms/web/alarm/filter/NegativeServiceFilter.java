/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.alarm.filter;

import javax.servlet.ServletContext;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.filter.NotEqualOrNullFilter;

/**
 * Encapsulates all service filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NegativeServiceFilter extends NotEqualOrNullFilter<Integer> {
    /** Constant <code>TYPE="servicenot"</code> */
    public static final String TYPE = "servicenot";

    /**
     * <p>Constructor for NegativeServiceFilter.</p>
     *
     * @param serviceId a int.
     */
    public NegativeServiceFilter(int serviceId) {
        super(TYPE, SQLType.INT, "SERVICEID", "serviceType.id", serviceId);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription(ServletContext servletContext) {
        String serviceName = Integer.toString(getValue());
        serviceName = NetworkElementFactory.getInstance(servletContext).getServiceNameFromId(getValue());

        return ("service is not " + serviceName);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<AlarmFactory.NegativeServiceFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getServiceId</p>
     *
     * @return a int.
     */
    public int getServiceId() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }

    @Override
    public String getTextDescription() {
        // TODO Auto-generated method stub
        return null;
    }
}
