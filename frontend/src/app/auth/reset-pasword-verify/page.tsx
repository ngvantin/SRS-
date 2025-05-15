'use client'

import { useState, useEffect } from 'react'
import Link from 'next/link'
import { useRouter, useSearchParams } from 'next/navigation'
import { CheckCircle, XCircle } from 'lucide-react'

export default function ResetPasswordVerifyPage() {
    const router = useRouter()
    const searchParams = useSearchParams()
    const token = searchParams.get('token')

    const [loading, setLoading] = useState(true)
    const [valid, setValid] = useState(false)
    const [error, setError] = useState('')

    useEffect(() => {
        const verifyToken = async () => {
            if (!token) {
                setValid(false)
                setError('Missing reset token')
                setLoading(false)
                return
            }

            try {
                // Here you would verify the token with your API
                // For example:
                // const response = await fetch(`/api/auth/verify-reset-token?token=${token}`)
                // const data = await response.json()
                // setValid(response.ok)

                // Simulate API call
                await new Promise(resolve => setTimeout(resolve, 1000))

                // For demo purposes, we'll consider the token valid
                setValid(true)
            } catch (err) {
                setError('Failed to verify reset token')
                setValid(false)
            } finally {
                setLoading(false)
            }
        }

        verifyToken()
    }, [token])

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
                <div className="max-w-md w-full text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                    <p className="mt-4 text-gray-600">Verifying your reset link...</p>
                </div>
            </div>
        )
    }

    if (!valid) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
                <div className="max-w-md w-full space-y-8 text-center">
                    <XCircle className="h-16 w-16 text-red-500 mx-auto" />
                    <h2 className="text-3xl font-extrabold text-gray-900">Invalid Reset Link</h2>
                    <p className="text-gray-600">
                        {error || 'The password reset link is invalid or has expired.'}
                    </p>
                    <div className="mt-6">
                        <Link
                            href="/auth/forgot-password"
                            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                        >
                            Request a new link
                        </Link>
                    </div>
                </div>
            </div>
        )
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8 text-center">
                <CheckCircle className="h-16 w-16 text-green-500 mx-auto" />
                <h2 className="text-3xl font-extrabold text-gray-900">Valid Reset Link</h2>
                <p className="text-gray-600">
                    Your password reset link is valid. You can now create a new password.
                </p>
                <div className="mt-6">
                    <Link
                        href={`/auth/reset-password?token=${token}`}
                        className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                        Continue to reset password
                    </Link>
                </div>
            </div>
        </div>
    )
}
