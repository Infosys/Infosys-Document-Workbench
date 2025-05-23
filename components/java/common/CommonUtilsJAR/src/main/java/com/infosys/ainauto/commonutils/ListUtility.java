/** =============================================================================================================== *
 * Copyright 2019 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */

package com.infosys.ainauto.commonutils;

import java.util.ArrayList;
import java.util.List;

public final class ListUtility {

	private ListUtility() {
		// private constructor to avoid instantiation
	}

	public static boolean hasValue(List<?> objectList) {
		return objectList != null && !objectList.isEmpty();
	}

	
	/**
	 * Method converts list to <b>n</b> number of partitioned sub lists 
	 * </br>
	 * <b>Eg: dataList = [1,2,3,4,5,6,7,8,9], noOfPartitions = 5 => [[1,6],[2,7],[3,8],[4,9],[5]]</b>
	 * @param dataList
	 * @param noOfPartitions
	 * @return
	 */
	public static <T> List<List<T>> convertListToPartitions(List<T> dataList, int noOfPartitions) {
		List<List<T>> partitionList = new ArrayList<>();
		for (int i = 0; i < dataList.size(); i++) {
			int index = i % noOfPartitions;
			if (partitionList.size() < index + 1) {
				List<T> newList = new ArrayList<>();
				partitionList.add(newList);
			}
			partitionList.get(index).add(dataList.get(i));
		}
		// return the lists
		return partitionList;
	}
	
	/**
	 * Method converts list to sub lists of size <b>n</b> each 
	 * </br>
	 * <b>Eg: dataList = [1,2,3,4,5,6,7,8,9], subListSize = 5 => [[1,2,3,4,5],[6,7,8,9]]</b>
	 * @param dataList
	 * @param subListSize
	 * @return
	 */
	public static <T> List<List<T>> convertListToSubListOfGivenSize(List<T> dataList, int subListSize) {
		// get size of the list
		int dataListSize = dataList.size();

		// calculate number of partitions m of size n each
		int noOfPartitions = dataListSize / subListSize;
		if (dataListSize % subListSize != 0)
			noOfPartitions++;

		// create m empty lists and initialize it using List.subList()
		List<List<T>> partitionList = new ArrayList<>();
		for (int i = 0; i < noOfPartitions; i++) {

			int fromIndex = i * subListSize;
			int toIndex = (i * subListSize + subListSize < dataListSize) ? (i * subListSize + subListSize)
					: dataListSize;

			List<T> partition = new ArrayList<>(dataList.subList(fromIndex, toIndex));
			partitionList.add(partition);
		}

		// return the lists
		return partitionList;
	}
	
	

}