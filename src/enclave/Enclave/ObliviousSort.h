#include <stdint.h>

#include "NewInternalTypes.h"
#include "EncryptedDAG.h"

#ifndef OBLIVIOUS_SORT_H
#define OBLIVIOUS_SORT_H

/**
 * Decrypt num_rows rows from buffer, sort them using sort_ptrs as scratch space, and re-encrypt
 * and write the result back into buffer. The length of sort_ptrs must be at least num_rows.
 */
template<typename RecordType>
uint32_t sort_single_buffer(
  int op_code, Verify *verify_set,
  uint8_t *buffer, uint8_t *buffer_end,
  uint8_t *write_buffer,
  uint32_t num_rows, SortPointer<RecordType> *sort_ptrs,
  uint32_t sort_ptrs_len, uint32_t row_upper_bound, uint32_t *num_comparisons,
  uint32_t *num_deep_comparisons);

/**
 * Decrypt two sorted, encrypted buffers, merge them using sort_ptrs as scratch space, and
 * re-encrypt and write the result back into buffer1 and buffer2, splitting it across the two
 * buffers. The length of sort_ptrs must be at least buffer1_rows + buffer2_rows.
 */
template<typename RecordType>
void merge(int op_code, Verify *verify,
  uint8_t *buffer1, uint32_t buffer1_rows, uint8_t *buffer2, uint32_t buffer2_rows,
  SortPointer<RecordType> *sort_ptrs, uint32_t sort_ptrs_len, uint32_t row_upper_bound,
  uint32_t *num_comparisons, uint32_t *num_deep_comparisons);

/**
 * Sort an arbitrary number of encrypted buffers and write the results back to the same buffers. The
 * number of rows in each buffer is specified in num_rows, which is a parallel array to buffer_list.
 * The buffers must be sized so that two of them can fit in enclave memory at the same time.
 *
 * This function is intended to be called with the buffers outside of enclave memory. It will
 * decrypt up to two at a time into enclave memory and operate on the decrypted copies, then
 * re-encrypt when writing the results back to the buffers.
 */
template<typename RecordType>
void external_oblivious_sort(
  int op_code, Verify *verify_set,
  uint32_t num_buffers, uint8_t **buffer_list, uint32_t row_upper_bound, uint8_t *single_buffer_scratch);

#include "ObliviousSort.tcc"

#endif
