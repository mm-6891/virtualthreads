request = function()
  return wrk.format("GET", "/api/credits/1")
end

done = function(summary, latency, requests)
  io.write("Completed responses: ", summary.requests, "\n")
  io.write("HTTP status errors (>399): ", summary.errors.status, "\n")
end