/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.criterion.Criterion;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * <p>Filter interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface Filter {

    /**
     * Returns a terse string (including a "=") that describes this filter in
     * such a way to easily be included in an HTTP GET parameter.
     *
     * <p>
     * Some examples:
     * <ul>
     * <li>"node=1"</li>
     * <li>"interface=192.168.0.1"</li>
     * <li>"severity=3"</li>
     * <li>"nodenamelike=opennms"</li>
     * </ul>
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription();

    /**
     * Returns a terse but human-readable string describing this filter in such
     * a way to easily be included in a search results list.
     *
     * <p>
     * Some examples (corresponding to the examples in
     * {@link #getDescription getDescription}):
     * <ul>
     * <li>"node=nodelabel_of_node_1"</li>
     * <li>"interface=192.168.0.1"</li>
     * <li>"severity=Normal"</li>
     * <li>"node name containing \"opennms\""</li>
     * </ul>
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription();
}
