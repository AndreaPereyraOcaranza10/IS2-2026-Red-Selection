const usdFormatter = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
});

export const formatUSD = (amount) => usdFormatter.format(Number(amount) || 0);
