export type FaultParty =
  | 'CARRIER_3PL_FAULT'
  | 'SELLER_MERCHANT_FAULT'
  | 'CUSTOMER_SUBJECTIVE_BIAS'
  | 'NO_VIOLATION';

export type ComplaintCategory =
  | 'LATE_DELIVERY'
  | 'DAMAGED_PACKAGE'
  | 'LOST_ITEM'
  | 'WRONG_OR_MISSING_PRODUCT'
  | 'RUDE_COURIER'
  | 'OTHER';

export interface SlaDisputeAuditDto {
  orderId: string;
  reviewScore: number;
  totalDeliveryDelayDays: number;
  sellerHandoverDelayDays: number;
  isSlaBreached: boolean;
  aiComplaintCategory: ComplaintCategory;
  aiBlamedParty: string;
  aiEvidenceSnippet: string;
  finalFaultParty: FaultParty;
  penaltyEligible: boolean;
  arbitrationReason: string;
}
