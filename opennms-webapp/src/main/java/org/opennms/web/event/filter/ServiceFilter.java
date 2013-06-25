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

package org.opennms.web.event.filter;

import javax.servlet.ServletContext;

import org.opennms.web.element.NetworkElementFactory;
import org.opennms.web.element.NetworkElementFactoryInterface;
import org.opennms.web.filter.EqualsFilter;
import org.springframework.context.ApplicationContext;

/**
 * Encapsulates all service filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ServiceFilter extends EqualsFilter<Integer> {
    /** Constant <code>TYPE="service"</code> */
    public static final String TYPE = "service";
    private ServletContext m_servletContext;
    private ApplicationContext m_appContext;

    /**
     * <p>Constructor for ServiceFilter.</p>
     *
     * @param serviceId a int.
     * @param servletContext
     */
    public ServiceFilter(int serviceId, ServletContext servletContext) {
        super(TYPE, SQLType.INT, "EVENTS.SERVICEID", "serviceType.id", serviceId);
        m_servletContext = servletContext;
    }

    /**
     * <p>Constructor for ServiceFilter.</p>
     *
     * @param serviceId a int.
     * @param appContext
     */
    public ServiceFilter(int serviceId, ApplicationContext appContext) {
        super(TYPE, SQLType.INT, "EVENTS.SERVICEID", "serviceType.id", serviceId);
        m_appContext = appContext;
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        NetworkElementFactoryInterface factory = (m_servletContext == null ? NetworkElementFactory.getInstance(m_appContext) : NetworkElementFactory.getInstance(m_servletContext));
        String serviceName = factory.getServiceNameFromId(getServiceId());

        return (TYPE + " is " + serviceName);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<WebEventRepository.ServiceFilter: " + this.getDescription() + ">");
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
}
