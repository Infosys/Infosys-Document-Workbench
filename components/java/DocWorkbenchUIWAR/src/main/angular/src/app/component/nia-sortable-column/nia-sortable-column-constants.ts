/** =============================================================================================================== *
 * Copyright 2018 Infosys Ltd.                                                                                    *
 * Use of this source code is governed by Apache License Version 2.0 that can be found in the LICENSE file or at    *
 * http://www.apache.org/licenses/ 
 * ================================================================================================================ *
 */


export const NIASCCONST = {
  SORT: {
    ORDER: {
      ASC: 'asc',
      DESC: 'desc',
      CUSTOM: 'custom'
    },
    MESSAGE: {
      DEFAULT: 'Click to Sort'
    }
  },
  ROW_VARIATION: {
    RELATIVE_PARENT: 'data-relative-parent',
    RELATIVE_TO_PARENT: 'data-relative-to-parent'
  },
  SELECTOR: {
    RELATIVE_PARENT: '#<<id>> tr[data-relative-parent]',
    RELATIVE_TO_PARENT: '#<<id>> tr[data-relative-to-parent]'
  },
  REPLACE_PLACEHOLDER: {
    ID: '<<id>>'
  },
  SEQ_DECORATOR: {
    DOT: '.'
  }
};
