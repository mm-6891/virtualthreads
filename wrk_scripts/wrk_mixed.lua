-- wrk_mixed.lua
counter = 0

request = function()
  counter = counter + 1
  if counter % 10 == 0 then
    -- Cada 10 peticiones, haz un GET a la lista de créditos
    return wrk.format("GET", "/api/credits")
  elseif counter % 2 == 0 then
    -- En las pares, haz un GET a un crédito concreto (id aleatorio entre 1 y 10000)
    local id = math.random(1, 10000)
    return wrk.format("GET", "/api/credits/" .. id)
  else
    -- En las impares, haz un POST para crear un crédito
    wrk.method = "POST"
    wrk.body   = '{"holder":"usuario1","quantity":1000.0}'
    wrk.headers["Content-Type"] = "application/json"
    return wrk.format(nil, "/api/credits")
  end
end