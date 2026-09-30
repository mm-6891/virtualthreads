package com.pildora.apps.virtualthreads.domain;

public record Credit(Long id, String holder, double quantity, String status) {
   public Credit setId(Long id) {
      return new Credit(id, this.holder, this.quantity, this.status);
   }
   public Credit setStatus(String status) {
      return new Credit(this.id, this.holder, this.quantity, status);
   }
}
