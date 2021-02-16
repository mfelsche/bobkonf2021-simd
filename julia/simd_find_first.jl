using SIMD
using BenchmarkTools
using InteractiveUtils

const SimdBytes32 = SIMD.Vec{32,UInt8}
# const movemask_bitmask = SimdBytes32((0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7))

function find_first_simd(needle::UInt8, haystack::Array{UInt8})
    l = length(haystack)
    loop_size = 32
    for idx in 1:loop_size:length(haystack) # TODO: scalar loop
        reg = @inbounds SIMD.vload(SimdBytes32, haystack, idx)
        res = reg == needle
        if SIMD.any(res) # TODO: this is costly
            # LLVM doesnt have a movemask
            for i in 1:32
                if @inbounds res[i]
                    return idx + i - 1
                end
            end
        end
    end
    nothing
end

function find_first_scalar_naive(needle::UInt8, haystack::Array{UInt8})
    for i in 1:length(haystack)
        @inbounds if haystack[i] == needle
            return i
        end
    end
    nothing
end

# this will use memchr
function find_first_scalar(needle::UInt8, haystack::Array{UInt8})
    findfirst(isequal(needle), haystack)
end


len = (32 * 32) - 1
s = cat(Vector{UInt8}(repeat("a", len)), b"b", dims=1)
println("scalar naive")
println(find_first_scalar_naive(0x62::UInt8, s))
# InteractiveUtils.@code_native debuginfo = :none find_first_scalar_naive(0x62::UInt8, s)
res = @benchmark find_first_scalar_naive(0x62::UInt8, $s)
println(repr("text/plain", res))

println("scalar")
println(find_first_scalar(0x62::UInt8, s))
# InteractiveUtils.@code_native debuginfo = :none find_first_scalar(0x62::UInt8, s)
res = @benchmark find_first_scalar(0x62::UInt8, $s)
println(repr("text/plain", res))
println()
println("simd")
println(find_first_simd(0x62::UInt8, s))
# InteractiveUtils.@code_native debuginfo = :none find_first_simd(0x62::UInt8, s)
res = @benchmark find_first_simd(0x62::UInt8, $s)
println(repr("text/plain", res))

