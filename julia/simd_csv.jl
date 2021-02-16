using SIMD

# detect structural data in csv file
# ',' comma      0x2c - bucket 01
# '"' quote      0x22 - bucket 02

# space          0x20
# '\n' newline   0x0a - bucket 04
# horizontal tab 0x09 - bucket 08
# carriage return 0x0d - bucket 16

const SimdBytes32 = SIMD.Vec{32,UInt8} 

structural_shufti_mask = SimdBytes32(0x03)
linebreak_shufti_mask  = SimdBytes32(0x1c)

high_nibble_mask = SimdBytes32((0x00, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,))
low_nibble_mask = SimdBytes32((0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,))
lower_bit_mask = mask = SimdBytes32(0x7f)
null_vec = SimdBytes32(0x00)

function get_structurals(xs::SimdBytes32)
    lo_shuffle = SIMD.shufflevector(low_nibble_mask, Val(convert(NTuple{32,UInt32}, NTuple{32,UInt8}(xs & 0x0f))))
    
    hi_nibbles = (xs >> 4) & lower_bit_mask
    v_lo = lo_shuffle & SIMD.shufflevector(high_nibble_mask, Val(convert(NTuple{32,UInt32}, NTuple{32,UInt8}(hi_nibbles))))
    tmp_lo = (v_lo & structural_shufti_mask) == null_vec
    # simulate movemask
    res::UInt32 = 0
    for i = 1:32
        if !getindex(tmp_lo, i)
            res |= 2^(i - 1)
        end
    end
    return res
end

function main()
    io = Base.open("./test.csv", "r")
    data_size = 4096
    chunk = Array{UInt8,1}(undef, data_size)

    # TODO: more efficient reading
    num_read = readbytes!(io, chunk, data_size)
    data_idx = 1
    read_until = data_idx + num_read
    num = 1
    while num_read > 0
        while data_idx + 32 <= read_until
            xs = @inbounds SIMD.vload(SimdBytes32, chunk, data_idx)
            mask = get_structurals(xs)
            println(num, ": ", bitstring(mask))
            data_idx += 32
            num += 1
        end
        
        # read next chunk and prepend leftover if necessary
        if data_idx <= read_until
            left_over = chunk[data_idx:end]
            num_read = readbytes!(io, chunk, data_size)
            if num_read > 0
                chunk = vcat(left_over, chunk)
            end
        else
            num_read = readbytes!(io, chunk, data_size)
        end
        
    end
    # "scalar tail"
    if data_idx <= read_until
        scratch = Array{UInt8,1}(undef, 32)
        # fill up with zeroes, extending the array
        for i = read_until:data_idx + 32
            chunk[i] = 0
        end
        xs = @inbounds SIMD.vload(SimdBytes32, chunk, data_idx)
        mask = get_structurals(xs)
        println(num, ": ", bitstring(mask))
    end
    close(io)
end

main()
