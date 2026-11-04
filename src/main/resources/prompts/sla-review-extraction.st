You are an objective E-Commerce Logistics Dispute Analyst.
Analyze the following customer review (originally in Portuguese/Vietnamese/English) for order `{orderId}`.

Customer Review Score: {reviewScore} / 5
Customer Review Comment:
"""
{reviewComment}
"""

Extract the complaint strictly into the requested JSON structure:
1. `complaintCategory`: Choose ONE of [LATE_DELIVERY, DAMAGED_PACKAGE, LOST_ITEM, WRONG_OR_MISSING_PRODUCT, RUDE_COURIER, OTHER].
2. `blamedParty`: Who is the customer blaming in the text? Choose ONE of [CARRIER_3PL, SELLER_MERCHANT, BOTH, UNCLEAR].
3. `evidenceSnippet`: Exact verbatim quote from the customer's review that proves the complaint. Do NOT invent text.

{format}
